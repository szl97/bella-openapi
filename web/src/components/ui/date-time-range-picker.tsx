'use client';

import * as React from "react";
import { format, subDays, startOfDay, endOfDay, startOfWeek, endOfWeek, startOfMonth, endOfMonth } from "date-fns";
import { Calendar as CalendarIcon, Clock } from "lucide-react";
import { DateRange } from "react-day-picker";
import { cn } from "@/lib/utils";
import { Button } from "@/components/ui/button";
import { Calendar } from "@/components/ui/calendar";
import {
  Popover,
  PopoverContent,
  PopoverTrigger,
} from "@/components/ui/popover";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";
import { Input } from "./input";
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs";

interface DateTimeRangePickerProps {
  startDate: Date;
  endDate: Date;
  onChange: (startDate: Date, endDate: Date) => void;
  maxDate?: Date;
  minDate?: Date;
}

export function DateTimeRangePicker({
  startDate,
  endDate,
  onChange,
  maxDate,
  minDate,
}: DateTimeRangePickerProps) {
  const [selectedRange, setSelectedRange] = React.useState<DateRange>({
    from: startDate,
    to: endDate,
  });

  // 预设时间范围选项
  const presets = [
    { label: "今天", getValue: () => {
      const today = new Date();
      return { 
        from: new Date(today.setHours(0, 0, 0, 0)), 
        to: new Date(new Date().setHours(23, 59, 59, 999)) 
      };
    }},
    { label: "昨天", getValue: () => {
      const yesterday = subDays(new Date(), 1);
      return { 
        from: new Date(yesterday.setHours(0, 0, 0, 0)), 
        to: new Date(yesterday.setHours(23, 59, 59, 999)) 
      };
    }},
    { label: "过去7天", getValue: () => {
      const end = new Date();
      end.setHours(23, 59, 59, 999);
      const start = subDays(new Date(), 6);
      start.setHours(0, 0, 0, 0);
      return { from: start, to: end };
    }},
    { label: "过去30天", getValue: () => {
      const end = new Date();
      end.setHours(23, 59, 59, 999);
      const start = subDays(new Date(), 29);
      start.setHours(0, 0, 0, 0);
      return { from: start, to: end };
    }},
    { label: "本周", getValue: () => {
      const start = startOfWeek(new Date(), { weekStartsOn: 1 });
      const end = endOfWeek(new Date(), { weekStartsOn: 1 });
      end.setHours(23, 59, 59, 999);
      return { from: start, to: end };
    }},
    { label: "本月", getValue: () => {
      const start = startOfMonth(new Date());
      const end = endOfMonth(new Date());
      end.setHours(23, 59, 59, 999);
      return { from: start, to: end };
    }},
    { label: "过去1小时", getValue: () => {
      const end = new Date();
      const start = new Date();
      start.setHours(start.getHours() - 1);
      return { from: start, to: end };
    }},
  ];

  // 处理预设选择
  const handlePresetChange = (preset: string) => {
    const selectedPreset = presets.find(p => p.label === preset);
    if (selectedPreset) {
      const range = selectedPreset.getValue();
      setSelectedRange(range);
      if (range.from && range.to) {
        onChange(range.from, range.to);
      }
    }
  };

  const handleDateRangeSelect = (range: DateRange | undefined) => {
    if (range?.from) {
      const newStartDate = new Date(range.from);
      newStartDate.setHours(startDate.getHours());
      newStartDate.setMinutes(startDate.getMinutes());

      let newEndDate = endDate;
      if (range.to) {
        newEndDate = new Date(range.to);
        newEndDate.setHours(endDate.getHours());
        newEndDate.setMinutes(endDate.getMinutes());
      }

      setSelectedRange({ from: newStartDate, to: newEndDate });
      onChange(newStartDate, newEndDate);
    }
  };

  // 格式化时间为 HH:mm 格式
  const formatTime = (date: Date | undefined) => {
    if (!date) return "";
    const hours = date.getHours().toString().padStart(2, "0");
    const minutes = date.getMinutes().toString().padStart(2, "0");
    return `${hours}:${minutes}`;
  };

  // 处理开始时间变更
  const handleStartTimeChange = (timeString: string) => {
    if (timeString && selectedRange.from) {
      const [hours, minutes] = timeString.split(':').map(Number);
      const newStartDate = new Date(selectedRange.from);
      newStartDate.setHours(hours || 0);
      newStartDate.setMinutes(minutes || 0);
      newStartDate.setSeconds(0);
      
      const newEndDate = selectedRange.to || endDate;
      
      // 验证开始时间不晚于结束时间
      if (selectedRange.to && newStartDate > newEndDate) {
        // 如果开始时间晚于结束时间，将结束时间设置为开始时间后1小时
        const adjustedEndDate = new Date(newStartDate);
        adjustedEndDate.setHours(adjustedEndDate.getHours() + 1);
        setSelectedRange({ from: newStartDate, to: adjustedEndDate });
        onChange(newStartDate, adjustedEndDate);
      } else {
        setSelectedRange({ from: newStartDate, to: newEndDate });
        onChange(newStartDate, newEndDate);
      }
    }
  };

  // 处理结束时间变更
  const handleEndTimeChange = (timeString: string) => {
    if (timeString && selectedRange.to) {
      const [hours, minutes] = timeString.split(':').map(Number);
      const newEndDate = new Date(selectedRange.to);
      newEndDate.setHours(hours || 0);
      newEndDate.setMinutes(minutes || 0);
      newEndDate.setSeconds(0);
      
      const newStartDate = selectedRange.from || startDate;
      
      // 验证结束时间不早于开始时间
      if (newEndDate < newStartDate) {
        // 如果结束时间早于开始时间，将开始时间设置为结束时间前1小时
        const adjustedStartDate = new Date(newEndDate);
        adjustedStartDate.setHours(adjustedStartDate.getHours() - 1);
        setSelectedRange({ from: adjustedStartDate, to: newEndDate });
        onChange(adjustedStartDate, newEndDate);
      } else {
        setSelectedRange({ ...selectedRange, to: newEndDate });
        onChange(newStartDate, newEndDate);
      }
    }
  };

  // 显示日期范围文本
  const dateRangeText = React.useMemo(() => {
    if (!selectedRange.from) return "选择日期范围";
    
    if (selectedRange.to) {
      return `${format(selectedRange.from, "yyyy-MM-dd HH:mm")} 至 ${format(selectedRange.to, "yyyy-MM-dd HH:mm")}`;
    }
    
    return format(selectedRange.from, "yyyy-MM-dd HH:mm");
  }, [selectedRange]);

  return (
    <div className="flex items-center gap-2">
      {/* 预设选择 */}
      <Select onValueChange={handlePresetChange}>
        <SelectTrigger className="h-9 w-[100px] border-gray-300">
          <SelectValue placeholder="快速选择" />
        </SelectTrigger>
        <SelectContent>
          {presets.map((preset) => (
            <SelectItem key={preset.label} value={preset.label}>
              {preset.label}
            </SelectItem>
          ))}
        </SelectContent>
      </Select>

      {/* 日期范围选择器 */}
      <Popover>
        <PopoverTrigger asChild>
          <Button
            variant="outline"
            className="h-9 border-gray-300 justify-start text-left font-normal w-[320px] text-sm"
          >
            <CalendarIcon className="mr-2 h-4 w-4" />
            {dateRangeText}
          </Button>
        </PopoverTrigger>
        <PopoverContent className="w-auto p-0" align="start">
          <Tabs defaultValue="calendar">
            <div className="flex items-center justify-between px-4 pt-3">
              <TabsList>
                <TabsTrigger value="calendar">日历</TabsTrigger>
                <TabsTrigger value="time">时间</TabsTrigger>
              </TabsList>
            </div>
            <TabsContent value="calendar" className="p-0">
              <Calendar
                initialFocus
                mode="range"
                defaultMonth={startDate}
                selected={selectedRange}
                onSelect={handleDateRangeSelect}
                numberOfMonths={2}
                disabled={(date) => {
                  if (maxDate && date > maxDate) return true;
                  if (minDate && date < minDate) return true;
                  return false;
                }}
              />
            </TabsContent>
            <TabsContent value="time" className="p-4 space-y-4">
              <div className="grid grid-cols-2 gap-4">
                <div className="space-y-2">
                  <div className="text-sm font-medium">开始时间</div>
                  <div className="flex items-center">
                    <Clock className="mr-2 h-4 w-4 text-gray-500" />
                    <Input
                      type="time"
                      value={formatTime(selectedRange.from)}
                      onChange={(e) => handleStartTimeChange(e.target.value)}
                      className="h-9"
                    />
                  </div>
                </div>
                <div className="space-y-2">
                  <div className="text-sm font-medium">结束时间</div>
                  <div className="flex items-center">
                    <Clock className="mr-2 h-4 w-4 text-gray-500" />
                    <Input
                      type="time"
                      value={formatTime(selectedRange.to)}
                      onChange={(e) => handleEndTimeChange(e.target.value)}
                      className="h-9"
                    />
                  </div>
                </div>
              </div>
            </TabsContent>
          </Tabs>
        </PopoverContent>
      </Popover>
    </div>
  );
}
