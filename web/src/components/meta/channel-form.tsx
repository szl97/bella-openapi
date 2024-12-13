import React, {useState} from 'react';
import {Channel} from '@/lib/types/openapi';
import {Card, CardContent, CardHeader, CardTitle} from "@/components/ui/card";
import {EditableField} from './editable-field';
import {Button} from "@/components/ui/button";
import {Badge} from "@/components/ui/badge";
import {ConfirmDialog} from '@/components/ui/confirm-dialog';
import {Label} from "@/components/ui/label";
import {Switch} from "@/components/ui/switch";

interface ChannelFormProps {
    channel: Channel;
    onUpdate: (channelCode: string, field: keyof Channel, value: string | number) => void;
    onToggleStatus: (channelCode: string) => void;
}

export function ChannelForm({ channel, onUpdate, onToggleStatus }: ChannelFormProps) {
    const [isStatusDialogOpen, setIsStatusDialogOpen] = useState(false);
    const [isTrialDialogOpen, setIsTrialDialogOpen] = useState(false);

    const handleToggleStatus = () => {
        setIsStatusDialogOpen(true);
    };

    const handleToggleTrial = () => {
        setIsTrialDialogOpen(true);
    };

    return (
        <Card className="bg-gradient-to-r from-blue-50 to-purple-50 shadow-sm">
            <CardHeader className="flex flex-col space-y-2">
                <div className="flex items-center justify-between">
                    <CardTitle className="text-gray-800">{channel.channelCode}</CardTitle>
                    <div className="flex items-center space-x-2">
                        <Badge variant={channel.trialEnabled === 1 ? "default" : "destructive"}>
                            {channel.trialEnabled === 1 ? '已支持试用' : '不支持试用'}
                        </Badge>
                        <Badge variant={channel.status === 'active' ? "default" : "destructive"}>
                            {channel.status === 'active' ? '已启用' : '已停用'}
                        </Badge>
                    </div>
                </div>
                <div className="flex justify-end space-x-2">
                    <Button
                        onClick={handleToggleTrial}
                        variant={channel.trialEnabled === 1 ? "destructive" : "default"}
                        size="sm"
                    >
                        {channel.trialEnabled === 1 ? '禁止试用' : '支持试用'}
                    </Button>
                    <Button
                        onClick={handleToggleStatus}
                        variant={channel.status === 'active' ? "destructive" : "default"}
                    >
                        {channel.status === 'active' ? '停用' : '启用'}
                    </Button>
                </div>
            </CardHeader>
            <CardContent className="space-y-6">
                <div className="space-y-2 bg-white bg-opacity-50 p-3 rounded-lg">
                    <Label className="text-sm font-medium text-gray-700">转发url</Label>
                    <div className="text-sm font-medium text-gray-700">{channel.url}</div>
                </div>
                <div className="space-y-2 bg-white bg-opacity-50 p-3 rounded-lg">
                    <Label className="text-sm font-medium text-gray-700">协议</Label>
                    <div className="text-sm font-medium text-gray-700">{channel.protocol}</div>
                </div>
                <div className="space-y-2 bg-white bg-opacity-50 p-3 rounded-lg">
                    <Label className="text-sm font-medium text-gray-700">供应商</Label>
                    <div className="text-sm font-medium text-gray-700">{channel.supplier}</div>
                </div>
                <EditableField
                    label="渠道信息"
                    value={channel.channelInfo}
                    onUpdate={(value) => onUpdate(channel.channelCode, 'channelInfo', value)}
                    multiline
                />
                <EditableField
                    label="价格信息"
                    value={channel.priceInfo}
                    onUpdate={(value) => onUpdate(channel.channelCode, 'priceInfo', value)}
                    multiline
                />
                <EditableField
                            label='优先级'
                            value={channel.priority}
                            onUpdate={(value) => onUpdate(channel.channelCode, 'priority', value)}
                />
                <ConfirmDialog
                    isOpen={isStatusDialogOpen}
                    onClose={() => setIsStatusDialogOpen(false)}
                    onConfirm={() => {
                        onToggleStatus(channel.channelCode);
                        setIsStatusDialogOpen(false);
                    }}
                    title={`确认${channel.status === 'active' ? '停用' : '启用'}渠道`}
                    description={`您确定要${channel.status === 'active' ? '停用' : '启用'}该渠道吗？`}
                />
                <ConfirmDialog
                    isOpen={isTrialDialogOpen}
                    onClose={() => setIsTrialDialogOpen(false)}
                    onConfirm={() => {
                        onUpdate(channel.channelCode, 'trialEnabled', channel.trialEnabled === 1 ? 0 : 1);
                        setIsTrialDialogOpen(false);
                    }}
                    title={`确认${channel.trialEnabled === 1 ? '禁止' : '支持'}试用`}
                    description={`您确定要${channel.trialEnabled === 1 ? '禁止' : '支持'}该渠道的试用吗？`}
                />
            </CardContent>
        </Card>
    );
}
