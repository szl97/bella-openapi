'use client';

import React, { useState, useEffect } from 'react';
import {
    getModelDetails,
    updateModel,
    updateChannel,
    updateModelStatus,
    updateModelVisibility,
    updateChannelStatus, linkModel
} from '@/lib/api/meta';
import { Model, Channel, ModelDetails } from '@/lib/types/openapi';
import {ChannelForm} from "@/components/meta/channel-form";
import {ModelForm} from "@/components/meta/model-form";
import {ClientHeader} from "@/components/user/client-header";
import {useToast} from "@/hooks/use-toast";
import Link from "next/link";
import {Button} from "@/components/ui/button";

export default function ModelConsolePage({ searchParams }: { searchParams: { modelName: string } }) {
    const {modelName} = searchParams;
    const [modelDetails, setModelDetails] = useState<ModelDetails | null>(null);
    const [isLoading, setIsLoading] = useState(true);
    const [showActiveOnly, setShowActiveOnly] = useState(true);
    const { toast } = useToast()
    useEffect(() => {
        if (modelName) {
            setIsLoading(true);
            getModelDetails(modelName)
                .then(setModelDetails)
                .finally(() => setIsLoading(false));
        }
    }, [modelName]);

    if (isLoading) return <div>Loading...</div>;
    if (!modelDetails) return <div>Model not found</div>;

    const handleModelUpdate = async (field: keyof Model, value: string) => {
        try {
            if (field == 'linkedTo') {
                await linkModel(modelName, value);
            } else {
                await updateModel(modelName, {...modelDetails.model, [field]: value});
            }
            setModelDetails(prev => prev ? {
                ...prev,
                model: {...prev.model, [field]: value}
            } : null);
        } catch (error) {
            console.error('Failed to update model:', error);
            // @ts-ignore
            toast({ title: "修改失败", description: error.error as string, variant: "destructive" })
        }
    };

    const handleStatusUpdate = async () => {
        try {
            const activate = modelDetails.model.status === 'inactive'
            await updateModelStatus(modelName, activate);
            setModelDetails(prev => prev ? {
                ...prev,
                model: {...prev.model, status : activate ? 'active' : 'inactive'}
            } : null);
        } catch (error) {
            console.error('Failed to update model:', error);
            // @ts-ignore
            toast({ title: "修改失败", description: error.error, variant: "destructive" })
        }
    };

    const handleVisibilityUpdate = async () => {
        try {
            const publish = modelDetails.model.visibility === 'private'
            await updateModelVisibility(modelName, publish);
            setModelDetails(prev => prev ? {
                ...prev,
                model: {...prev.model, visibility : publish ? 'public' : 'private'}
            } : null);
        } catch (error) {
            console.error('Failed to update model:', error);
            // @ts-ignore
            toast({ title: "修改失败", description: error.error, variant: "destructive" })
        }
    };

    const handleChannelUpdate = async (channelCode: string, field: keyof Channel, value: string | number) => {
        try {
            const channelToUpdate = modelDetails.channels.find(ch => ch.channelCode === channelCode);
            if (!channelToUpdate) {
                console.error('Channel not found');
                return;
            }
            await updateChannel(channelCode, {...channelToUpdate, [field]: value});
            setModelDetails(prev => prev ? {
                ...prev,
                channels: prev.channels.map(ch =>
                    ch.channelCode === channelCode ? {...ch, [field]: value} : ch
                )
            } : null);
        } catch (error) {
            console.error('Failed to update channel:', error);
            // @ts-ignore
            toast({ title: "修改失败", description: error.error, variant: "destructive" })
        }
    };

    const handleChannelStatusUpdate = async (channelCode: string) => {
        try {
            const channelToUpdate = modelDetails.channels.find(ch => ch.channelCode === channelCode);
            if (!channelToUpdate) {
                console.error('Channel not found');
                return;
            }
            const activate = channelToUpdate.status === 'inactive'
            await updateChannelStatus(channelCode, activate);
            setModelDetails(prev => prev ? {
                ...prev,
                channels: prev.channels.map(ch =>
                    ch.channelCode === channelCode ? {...ch, status : activate ? 'active' : 'inactive' } : ch
                )
            } : null);
        } catch (error) {
            console.error('Failed to update channel:', error);
            // @ts-ignore
            toast({ title: "修改失败", description: error.error, variant: "destructive" })
        }
    };

    return (
        <div className="min-h-screen bg-gray-50">
            <ClientHeader title='模型管理'/>
            <div className="container mx-auto p-4">
                <h1 className="text-2xl font-bold mb-4">模型: {modelDetails.model.modelName}</h1>
                <ModelForm model={modelDetails.model} onUpdate={handleModelUpdate} onToggleStatus={handleStatusUpdate}
                           onToggleVisibility={handleVisibilityUpdate}/>

                <div className="flex justify-between items-center mt-12 mb-6">
                    <div className="flex items-center gap-4">
                        <h2 className="text-xl font-bold">渠道</h2>
                        <div className="flex items-center gap-2">
                            <Button
                                variant="outline"
                                onClick={() => setShowActiveOnly(false)}
                                className={!showActiveOnly ? "bg-black text-white hover:bg-black/90" : ""}
                            >
                                显示所有渠道
                            </Button>
                            <Button
                                variant="outline"
                                onClick={() => setShowActiveOnly(true)}
                                className={showActiveOnly ? "bg-black text-white hover:bg-black/90" : ""}
                            >
                                显示可用渠道
                            </Button>
                        </div>
                    </div>
                    <Link href={`/meta/console/channel?entityType=model&entityCode=${modelName}`}>
                        <Button
                            className="bg-purple-100 hover:bg-purple-200 text-gray-800 font-bold py-2 px-4 rounded-lg shadow-md transition duration-300 ease-in-out transform hover:scale-105">
                            <svg xmlns="http://www.w3.org/2000/svg" className="h-5 w-5 mr-2 inline-block"
                                 viewBox="0 0 20 20" fill="currentColor">
                                <path fillRule="evenodd"
                                      d="M10 3a1 1 0 011 1v5h5a1 1 0 110 2h-5v5a1 1 0 11-2 0v-5H4a1 1 0 110-2h5V4a1 1 0 011-1z"
                                      clipRule="evenodd"/>
                            </svg>
                            添加渠道
                        </Button>
                    </Link>
                </div>

                {modelDetails.channels
                    .filter(channel => !showActiveOnly || channel.status === 'active')
                    .map(channel => (
                    <div key={channel.channelCode} className="mb-6">
                        <ChannelForm channel={channel} onUpdate={handleChannelUpdate}
                                     onToggleStatus={handleChannelStatusUpdate}/>
                    </div>
                ))}
            </div>
        </div>
    );
}
