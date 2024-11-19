import React, { useState } from 'react';
import { Channel } from '@/lib/types/openapi';
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { EditableField } from './editable-field';
import { Button } from "@/components/ui/button";
import { Badge } from "@/components/ui/badge";
import { ConfirmDialog } from '@/components/ui/confirm-dialog';

interface ChannelFormProps {
    channel: Channel;
    onUpdate: (channelCode: string, field: keyof Channel, value: string) => void;
    onToggleStatus: (channelCode: string) => void;
}

export function ChannelForm({ channel, onUpdate, onToggleStatus }: ChannelFormProps) {
    const [isStatusDialogOpen, setIsStatusDialogOpen] = useState(false);

    const handleToggleStatus = () => {
        setIsStatusDialogOpen(true);
    };

    return (
        <Card className="bg-gradient-to-r from-blue-50 to-purple-50 shadow-sm">
            <CardHeader className="flex flex-col space-y-2">
                <div className="flex items-center justify-between">
                    <CardTitle className="text-gray-800">{channel.channelCode}</CardTitle>
                    <Badge variant={channel.status === 'active' ? "default" : "destructive"}>
                        {channel.status === 'active' ? '已启用' : '已停用'}
                    </Badge>
                </div>
                <div className="flex justify-end">
                    <Button
                        onClick={handleToggleStatus}
                        variant={channel.status === 'active' ? "destructive" : "default"}
                    >
                        {channel.status === 'active' ? '停用' : '启用'}
                    </Button>
                </div>
            </CardHeader>
            <CardContent className="space-y-4">
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
                    label="优先级"
                    value={channel.priority}
                    onUpdate={(value) => onUpdate(channel.channelCode, 'priority', value)}
                />
            </CardContent>
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
        </Card>
    );
}
