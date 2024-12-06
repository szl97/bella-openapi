'use client';

import {Suspense, useEffect, useState} from 'react';
import {MetricsLineChart} from '@/components/ui/metrics-line-chart';
import {ClientHeader} from "@/components/user/client-header";
import {DateTimeRangePicker} from "@/components/ui/date-time-range-picker";
import {format, subDays, subMinutes} from 'date-fns';
import {Sidebar} from '@/components/meta/sidebar';
import {getAllCategoryTrees, listModels, listConsoleModels} from '@/lib/api/meta';
import {hasPermission} from "@/lib/api/userInfo";
import {useUser} from "@/lib/context/user-context";
import {CategoryTree, Model, MonitorData} from "@/lib/types/openapi";
import {Input} from "@/components/ui/input";
import {ScrollArea} from "@/components/ui/scroll-area";
import {Search} from "lucide-react";

// 预定义的颜色数组
const colors = [
  '#6366f1', // Indigo
  '#ec4899', // Pink
  '#10b981', // Emerald
  '#f59e0b', // Amber
  '#3b82f6', // Blue
  '#8b5cf6', // Purple
  '#ef4444', // Red
  '#14b8a6', // Teal
  '#f97316', // Orange
  '#84cc16', // Lime
  '#06b6d4', // Cyan
  '#d946ef', // Fuchsia
  '#0ea5e9', // Light Blue
  '#22c55e', // Green
  '#eab308', // Yellow
  '#a855f7', // Purple
  '#f43f5e', // Rose
  '#64748b', // Slate
  '#6b7280', // Gray
  '#78716c', // Stone
];

// 动态生成颜色映射
const getChannelColors = (channels: string[]) => {
  const colorMap: { [key: string]: string } = {};
  // 对 channels 进行排序，确保相同的 channel 总是获得相同的颜色
  const sortedChannels = [...channels].sort();
  sortedChannels.forEach((channel, index) => {
    colorMap[channel] = colors[index % colors.length];
  });
  return colorMap;
};

// 转换数据格式
const transformData = (data: MonitorData[], metricType: keyof MonitorData['metrics']): { time: string; channels: { [key: string]: { value: number; status: number; rawData?: number[] } } }[] => {
  // 按时间分组
  const timeGroups = data.reduce((acc, item) => {
    // 确保时间格式为 YYYYMMDDHHMM
    const time = item.time.replace(/[-: ]/g, '').slice(0, 12);
    const timeGroup = acc.get(time) || [];
    timeGroup.push(item);
    acc.set(time, timeGroup);
    return acc;
  }, new Map<string, MonitorData[]>());

  // 转换为 MetricsData 格式
  return Array.from<[string, MonitorData[]]>(timeGroups.entries()).map(([time, items]) => {
    const channels: { [key: string]: { value: number; status: number; rawData?: number[] } } = {};

    items.forEach(item => {
      const value = item.metrics[metricType];
      channels[item.channel_code] = {
        value: metricType === 'status' ? (value === 1 ? 0 : 1) : value,
        status: item.metrics.status === 0 ? 0 : 1,
        rawData: [metricType === 'status' ? (value === 1 ? 0 : 1) : value]
      };
    });

    return {
      time,
      channels
    };
  }).sort((a, b) => a.time.localeCompare(b.time));
};

// 获取所有唯一的渠道
const getUniqueChannels = (data: MonitorData[]) => {
  return Array.from(new Set(data.map(item => item.channel_code))).sort();
};

function MonitorPageContent({ params }: { params: { model: string } }) {
  const { userInfo } = useUser();
  const [selectedEndpoint, setSelectedEndpoint] = useState<string>('/v1/chat/completions');
  const [selectedModel, setSelectedModel] = useState<string>(params.model);
  const [availableModels, setAvailableModels] = useState<Model[]>([]);
  const [searchQuery, setSearchQuery] = useState('');
  const [showModelList, setShowModelList] = useState(false);
  const [categoryTrees, setCategoryTrees] = useState<CategoryTree[]>([]);
  const [startDate, setStartDate] = useState(subMinutes(new Date(), 30));
  const [endDate, setEndDate] = useState(new Date());
  const [currentData, setCurrentData] = useState<MonitorData[]>([]);
  const [channels, setChannels] = useState<string[]>([]);
  const [selectedChannels, setSelectedChannels] = useState<string[]>([]);
  const [channelColors, setChannelColors] = useState<{ [key: string]: string }>({});
  const [intervalMinutes, setIntervalMinutes] = useState<number>(1);
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    async function fetchCategoryTrees() {
      const trees = await getAllCategoryTrees();
      setCategoryTrees(trees);
    }
    fetchCategoryTrees();
  }, []);

  useEffect(() => {
    async function fetchModels() {
      try {
        const models = hasPermission(userInfo, '/console/model/**')
          ? await listConsoleModels(selectedEndpoint, '', '', 'active', '')
          : await listModels(selectedEndpoint);
        setAvailableModels(models || []);
        const validModel = models?.find(m => m.modelName === selectedModel) || models?.[0];
        if (validModel) {
          setSelectedModel(validModel.terminalModel ? validModel.terminalModel : validModel.modelName);
        }
      } catch (error) {
        console.error('Error fetching models:', error);
        setAvailableModels([]);
      }
    }
    if (userInfo) {
      fetchModels();
    }
  }, [selectedEndpoint, userInfo]);

  const filteredModels = availableModels.filter(model =>
    model.modelName.toLowerCase().includes(searchQuery.toLowerCase())
  );

  useEffect(() => {
    if (!selectedModel) return;  // 如果没有选中的模型，不获取数据

    async function fetchData() {
      setIsLoading(true);
      setError(null);
      try {
        const response = await fetch('/api/metrics?' + new URLSearchParams({
          model: selectedModel,
          endpoint: selectedEndpoint,
          start: format(startDate, "yyyyMMddHHmm"),
          end: format(endDate, "yyyyMMddHHmm")
        }));
        const data = await response.json();
        setCurrentData(data);
      } catch (err) {
        setError(err instanceof Error ? err.message : 'Failed to fetch metrics data');
        console.error('Error fetching metrics:', err);
      } finally {
        setIsLoading(false);
      }
    }
    fetchData();
  }, [selectedModel, selectedEndpoint, startDate, endDate]);

  useEffect(() => {
    const uniqueChannels = getUniqueChannels(currentData);
    setChannels(uniqueChannels);
    setSelectedChannels([]); // 默认不选择任何渠道
    setChannelColors(getChannelColors(uniqueChannels));
  }, [currentData]);

  const handleDateRangeChange = (newStartDate: Date, newEndDate: Date) => {
    // 确保结束时间不早于开始时间
    if (newEndDate < newStartDate) {
      return;
    }

    // 确保时间范围不超过30天
    const thirtyDaysAgo = subDays(new Date(), 30);
    if (newStartDate < thirtyDaysAgo) {
      return;
    }

    // 确保结束时间不晚于当前时间
    const now = new Date();
    if (newEndDate > now) {
      newEndDate = now;
    }

    // 计算新的时间范围（分钟）
    const minutesDiff = (newEndDate.getTime() - newStartDate.getTime()) / (1000 * 60);

    // 检查时间范围/时间间隔是否大于60
    if (minutesDiff / intervalMinutes > 60 && intervalMinutes < 720) {
      // 如果大于60，调整时间间隔为合法的最小值
      const newIntervalMinutes = Math.ceil(minutesDiff / 60);
      setIntervalMinutes(newIntervalMinutes);
    }

    setStartDate(newStartDate);
    setEndDate(newEndDate);
  };

  // 时间间隔选项
  const intervalOptions = [
    { value: 1, label: '1分钟' },
    { value: 5, label: '5分钟' },
    { value: 15, label: '15分钟' },
    { value: 30, label: '30分钟' },
    { value: 60, label: '1小时' },
    { value: 180, label: '3小时' },
    { value: 360, label: '6小时' },
    { value: 720, label: '12小时' },
    { value: 1440, label: '1天' },
  ];

  // 处理时间间隔变化
  const handleIntervalChange = (newInterval: number) => {
    const minutesDiff = (endDate.getTime() - startDate.getTime()) / (1000 * 60);

    // 如果新的时间间隔会导致时间范围/时间间隔大于60，且不是12小时或1天，则不允许更改
    if (minutesDiff / newInterval > 60 && newInterval < 720) {
      return;
    }

    setIntervalMinutes(newInterval);
  };

  const metrics: any = {
    completed: transformData(currentData, 'completed'),
    ttlt: transformData(currentData, 'ttlt'),
    ttft: transformData(currentData, 'ttft'),
    errors: transformData(currentData, 'errors'),
    output_token: transformData(currentData, 'output_token'),
    input_token: transformData(currentData, 'input_token'),
    status: transformData(currentData, 'status'),
  };

  return (
    <div className="min-h-screen bg-gray-50">
      <ClientHeader title="能力点监控" />
      <div className="flex">
        <Sidebar
          categoryTrees={categoryTrees}
          onEndpointSelect={setSelectedEndpoint}
          defaultEndpoint={selectedEndpoint}
        />
        <main className="flex-1">
          <div className="p-6">
            <div className="bg-white p-6 rounded-lg shadow-sm space-y-4">
              <div className="flex flex-col space-y-4">
                <div className="flex items-center space-x-4">
                  <span className="text-sm font-medium text-gray-700">模型:</span>
                  <div className="relative w-[300px]">
                    <div
                      className="flex items-center border rounded-md p-2 cursor-pointer"
                      onClick={() => setShowModelList(!showModelList)}
                    >
                      <span className="flex-1">
                        {selectedModel || "选择模型..."}
                      </span>
                      <Search className="h-4 w-4 text-gray-500" />
                    </div>

                    {showModelList && (
                      <div className="absolute w-full mt-1 bg-white border rounded-md shadow-lg z-50">
                        <div className="p-2">
                          <Input
                            placeholder="搜索模型..."
                            value={searchQuery}
                            onChange={(e) => setSearchQuery(e.target.value)}
                            onClick={(e) => e.stopPropagation()}
                          />
                        </div>
                        <ScrollArea className="h-[200px]">
                          <div className="p-2">
                            {filteredModels.map((model) => (
                              <div
                                key={model.modelName}
                                className={`p-2 cursor-pointer hover:bg-gray-100 rounded-md ${
                                  selectedModel === model.modelName ? 'bg-gray-100' : ''
                                }`}
                                onClick={() => {
                                  setSelectedModel(model.terminalModel ? model.terminalModel : model.modelName);
                                  setShowModelList(false);
                                  setSearchQuery('');
                                }}
                              >
                                {model.modelName}
                              </div>
                            ))}
                            {filteredModels.length === 0 && (
                              <div className="p-2 text-gray-500 text-center">
                                未找到模型
                              </div>
                            )}
                          </div>
                        </ScrollArea>
                      </div>
                    )}
                  </div>
                </div>

                <div className="flex items-center space-x-4">
                  <span className="text-sm font-medium text-gray-700">时间范围:</span>
                  <DateTimeRangePicker
                    startDate={startDate}
                    endDate={endDate}
                    onChange={handleDateRangeChange}
                    maxDate={new Date()}
                    minDate={subDays(new Date(), 30)}
                  />
                </div>

                <div className="flex items-center space-x-4">
                  <span className="text-sm font-medium text-gray-700">时间间隔:</span>
                  <select
                    className="bg-white border rounded-md p-2"
                    value={intervalMinutes}
                    onChange={(e) => handleIntervalChange(Number(e.target.value))}
                  >
                    {intervalOptions
                      .filter(option => {
                        const minutesDiff = (endDate.getTime() - startDate.getTime()) / (1000 * 60);
                        // 如果时间间隔小于12小时，只显示不会导致时间范围/时间间隔大于60的选项
                        // 12小时和1天的选项始终显示
                        return (minutesDiff / option.value <= 60) || option.value >= 720;
                      })
                      .map(option => (
                        <option key={option.value} value={option.value}>
                          {option.label}
                        </option>
                      ))}
                  </select>
                </div>

                <div className="flex flex-col space-y-2">
                  <span className="text-sm font-medium text-gray-700">渠道:</span>
                  <div className="flex flex-wrap gap-2">
                    {channels.map(channel => (
                      <div
                        key={channel}
                        className={`flex items-center space-x-2 p-2 rounded-md cursor-pointer ${
                          selectedChannels.includes(channel) ? 'bg-gray-100' : ''
                        }`}
                        onClick={() => {
                          setSelectedChannels(prev =>
                            prev.includes(channel)
                              ? prev.filter(c => c !== channel)
                              : [...prev, channel]
                          );
                        }}
                      >
                        <div
                          className="w-3 h-3 rounded-full"
                          style={{ backgroundColor: channelColors[channel] }}
                        />
                        <span className="text-sm text-gray-600">{channel}</span>
                      </div>
                    ))}
                  </div>
                </div>
              </div>
            </div>

            <div className="grid grid-cols-1 md:grid-cols-2 gap-6 mt-6">
            <Suspense fallback={<div className="bg-white p-4 rounded-lg shadow-sm">Loading chart...</div>}>
                <MetricsLineChart
                  title="渠道状态（0:可用 1: 不可用）"
                  data={metrics.status}
                  channels={selectedChannels.length > 0 ? selectedChannels : channels}
                  channelColors={channelColors}
                  valueFormatter={(value: number) => value.toFixed(2)}
                  aggregationType="average"
                  intervalMinutes={intervalMinutes}
                />
              </Suspense>
              <Suspense fallback={<div className="bg-white p-4 rounded-lg shadow-sm">Loading chart...</div>}>
                <MetricsLineChart
                  title="请求总数"
                  data={metrics.completed}
                  channels={selectedChannels.length > 0 ? selectedChannels : channels}
                  channelColors={channelColors}
                  valueFormatter={(value: number) => Math.round(value).toString()}
                  aggregationType="sum"
                  intervalMinutes={intervalMinutes}
                />
              </Suspense>
              <Suspense fallback={<div className="bg-white p-4 rounded-lg shadow-sm">Loading chart...</div>}>
                <MetricsLineChart
                  title="异常请求数（httpcode >= 500）"
                  data={metrics.errors}
                  channels={selectedChannels.length > 0 ? selectedChannels : channels}
                  channelColors={channelColors}
                  valueFormatter={(value: number) => Math.round(value).toString()}
                  aggregationType="sum"
                  intervalMinutes={intervalMinutes}
                />
              </Suspense>
              <Suspense fallback={<div className="bg-white p-4 rounded-lg shadow-sm">Loading chart...</div>}>
                <MetricsLineChart
                  title="每分钟输出token"
                  data={metrics.output_token}
                  channels={selectedChannels.length > 0 ? selectedChannels : channels}
                  channelColors={channelColors}
                  valueFormatter={(value: number) => Math.round(value).toString()}
                  aggregationType="average"
                  intervalMinutes={intervalMinutes}
                />
              </Suspense>
              <Suspense fallback={<div className="bg-white p-4 rounded-lg shadow-sm">Loading chart...</div>}>
                <MetricsLineChart
                  title="每分钟输入token"
                  data={metrics.input_token}
                  channels={selectedChannels.length > 0 ? selectedChannels : channels}
                  channelColors={channelColors}
                  valueFormatter={(value: number) => Math.round(value).toString()}
                  aggregationType="average"
                  intervalMinutes={intervalMinutes}
                />
              </Suspense>
              {/* <Suspense fallback={<div className="bg-white p-4 rounded-lg shadow-sm">Loading chart...</div>}>
                <MetricsLineChart
                  title="响应时间 (s)"
                  data={metrics.ttlt}
                  channels={selectedChannels.length > 0 ? selectedChannels : channels}
                  channelColors={channelColors}
                  valueFormatter={(value: number) => `${Math.round(value)}s`}
                  aggregationType="average"
                  intervalMinutes={intervalMinutes}
                />
              </Suspense> */}
            </div>
          </div>
        </main>
      </div>
    </div>
  );
}

export default function MonitorPage(props: { params: { model: string } }) {
  return <MonitorPageContent {...props} />;
}
