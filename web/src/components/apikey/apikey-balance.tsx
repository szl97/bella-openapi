import React, { useState, useEffect } from 'react';
import { getApiKeyBalance } from '@/lib/api/apikey';
import { ApiKeyBalance } from '@/lib/types/apikey-balance';
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogHeader,
  DialogTitle,
} from "@/components/ui/dialog";
import { Button } from '@/components/ui/button';
import { Badge } from '@/components/ui/badge';
import { CircleDollarSign, AlertCircle, CheckCircle2 } from 'lucide-react';

interface ApiKeyBalanceDialogProps {
  code: string;
  isOpen: boolean;
  onClose: () => void;
}

interface ApiKeyBalanceIndicatorProps {
  code: string;
}

export const ApiKeyBalanceIndicator: React.FC<ApiKeyBalanceIndicatorProps> = ({ code }) => {
  const [balance, setBalance] = useState<ApiKeyBalance | null>(null);
  const [loading, setLoading] = useState<boolean>(true);
  const [error, setError] = useState<boolean>(false);

  useEffect(() => {
    if (code) {
      setLoading(true);
      setError(false);
      getApiKeyBalance(code)
        .then((data) => {
          if (data) {
            setBalance(data);
          } else {
            setError(true);
          }
        })
        .catch(() => {
          setError(true);
        })
        .finally(() => {
          setLoading(false);
        });
    }
  }, [code]);

  if (loading) {
    return <div className="flex justify-center"><div className="h-5 w-5 animate-pulse bg-gray-200 rounded-full"></div></div>;
  }

  if (error || !balance) {
    return <Badge variant="outline" className="bg-gray-100 text-gray-500">未知</Badge>;
  }

  const usagePercentage = (balance.cost / balance.quota) * 100;
  
  if (usagePercentage >= 80) {
    return (
      <Badge className="bg-red-100 text-red-800 flex items-center gap-1">
        <AlertCircle className="h-3 w-3" /> 余额紧张 ({Math.round(usagePercentage)}%)
      </Badge>
    );
  }
  
  return <Badge className="bg-green-100 text-green-800 flex items-center gap-1"><CheckCircle2 className="h-3 w-3" /> 余额充足</Badge>;
};

interface ApiKeyBalanceSummaryProps {
  apikeys: ApikeyInfo[];
}

export const ApiKeyBalanceSummary: React.FC<ApiKeyBalanceSummaryProps> = ({ apikeys }) => {
  const [balances, setBalances] = useState<ApiKeyBalance[]>([]);
  const [loading, setLoading] = useState<boolean>(true);

  useEffect(() => {
    if (apikeys && apikeys.length > 0) {
      setLoading(true);
      Promise.all(
        apikeys.map(apikey => getApiKeyBalance(apikey.code))
      ).then(results => {
        setBalances(results.filter(result => result !== null) as ApiKeyBalance[]);
      }).finally(() => {
        setLoading(false);
      });
    }
  }, [apikeys]);

  if (loading) {
    return <div className="h-10 animate-pulse bg-gray-100 rounded-md w-full max-w-md"></div>;
  }

  if (balances.length === 0) {
    return null;
  }

  const totalQuota = balances.reduce((sum, balance) => sum + balance.quota, 0);
  const totalUsed = balances.reduce((sum, balance) => sum + balance.cost, 0);
  const totalRemaining = totalQuota - totalUsed;
  const usagePercentage = (totalUsed / totalQuota) * 100;
  
  let statusColor = "bg-green-100 text-green-800";
  let statusText = "正常";
  let statusIcon = <CheckCircle2 className="h-4 w-4 mr-1" />;
  
  if (usagePercentage >= 80) {
    statusColor = "bg-red-100 text-red-800";
    statusText = "紧张";
    statusIcon = <AlertCircle className="h-4 w-4 mr-1" />;
  } else if (usagePercentage >= 60) {
    statusColor = "bg-yellow-100 text-yellow-800";
    statusText = "注意";
    statusIcon = <AlertCircle className="h-4 w-4 mr-1" />;
  }

  return (
    <div className="bg-white rounded-lg shadow p-4 max-w-2xl">
      <div className="flex items-center mb-2">
        <CircleDollarSign className="h-5 w-5 mr-2 text-gray-700" />
        <h3 className="text-lg font-medium">API Key 余额摘要</h3>
      </div>
      <div className="grid grid-cols-3 gap-4 mt-3">
        <div className="text-center">
          <div className="text-sm text-gray-500">总额度</div>
          <div className="font-semibold text-lg">{new Intl.NumberFormat("zh-CN", { style: "currency", currency: "CNY" }).format(totalQuota)}</div>
        </div>
        <div className="text-center">
          <div className="text-sm text-gray-500">已使用</div>
          <div className="font-semibold text-lg">{new Intl.NumberFormat("zh-CN", { style: "currency", currency: "CNY" }).format(totalUsed)}</div>
        </div>
        <div className="text-center">
          <div className="text-sm text-gray-500">剩余额度</div>
          <div className={`font-semibold text-lg ${totalRemaining < totalQuota * 0.2 ? 'text-red-600' : 'text-green-600'}`}>
            {new Intl.NumberFormat("zh-CN", { style: "currency", currency: "CNY" }).format(totalRemaining)}
          </div>
        </div>
      </div>
      <div className="mt-3 flex justify-between items-center">
        <div className="bg-gray-100 h-2 rounded-full flex-grow mr-4">
          <div 
            className={`h-full rounded-full ${usagePercentage >= 80 ? 'bg-red-500' : usagePercentage >= 60 ? 'bg-yellow-500' : 'bg-green-500'}`} 
            style={{ width: `${Math.min(usagePercentage, 100)}%` }}
          ></div>
        </div>
        <Badge className={`${statusColor} capitalize flex items-center`}>
          {statusIcon} 余额{statusText}
        </Badge>
      </div>
    </div>
  );
};

export const ApiKeyBalanceDialog: React.FC<ApiKeyBalanceDialogProps> = ({ 
  code, 
  isOpen, 
  onClose 
}) => {
  const [balance, setBalance] = useState<ApiKeyBalance | null>(null);
  const [loading, setLoading] = useState<boolean>(false);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    if (isOpen && code) {
      setLoading(true);
      setError(null);
      getApiKeyBalance(code)
        .then((data) => {
          if (data) {
            setBalance(data);
          } else {
            setError('无法获取余额信息');
          }
        })
        .catch(err => {
          console.error('Failed to fetch balance:', err);
          setError('获取余额信息时发生错误');
        })
        .finally(() => {
          setLoading(false);
        });
    }
  }, [isOpen, code]);

  return (
    <Dialog open={isOpen} onOpenChange={onClose}>
      <DialogContent className="sm:max-w-md">
        <DialogHeader>
          <DialogTitle>API Key 余额详情</DialogTitle>
          <DialogDescription>当前月份的使用情况和剩余额度</DialogDescription>
        </DialogHeader>
        <div className="py-4">
          {loading ? (
            <div className="flex justify-center items-center p-8">
              <div className="animate-spin rounded-full h-8 w-8 border-4 border-blue-500 border-t-transparent"></div>
            </div>
          ) : error ? (
            <div className="flex items-center justify-center text-red-500 p-4"><AlertCircle className="h-5 w-5 mr-2" />{error}</div>
          ) : balance && (
            <div className="space-y-4 p-4 bg-gray-50 rounded-lg border border-gray-200">
              <div className="grid grid-cols-2 gap-4 text-sm">
                <div className="font-medium text-gray-500">月份</div>
                <div>{balance.month}</div>
                <div className="font-medium text-gray-500">总额度</div>
                <div>{new Intl.NumberFormat("zh-CN", { style: "currency", currency: "CNY" }).format(balance.quota)}</div>
                <div className="font-medium text-gray-500">已使用</div>
                <div>{new Intl.NumberFormat("zh-CN", { style: "currency", currency: "CNY" }).format(balance.cost)}</div>
                <div className="font-medium text-gray-500">剩余额度</div>
                <div className={`font-semibold ${balance.balance < balance.quota * 0.2 ? 'text-red-600' : 'text-green-600'}`}>
                  {new Intl.NumberFormat("zh-CN", { style: "currency", currency: "CNY" }).format(balance.balance)}
                </div>
              </div>
              <div className={`text-sm mt-4 p-2 rounded ${balance.balance < balance.quota * 0.2 ? 'bg-red-50 text-red-800' : 'bg-green-50 text-green-800'}`}>
                {balance.balance < balance.quota * 0.2 ? (
                  <p className="flex items-center"><AlertCircle className="h-4 w-4 mr-2" />余额不足，请考虑增加额度</p>
                ) : (
                  <p>余额充足</p>
                )}
              </div>
            </div>
          )}
        </div>
        <div className="flex justify-end">
          <Button onClick={onClose}>关闭</Button>
        </div>
      </DialogContent>
    </Dialog>
  );
};
