import React, {ChangeEvent, useState} from 'react';
import {deleteApikey, resetApikey, updateCertify, updateQuota} from "@/app/api/apikey";
import {
    Dialog, DialogClose,
    DialogContent,
    DialogDescription, DialogFooter,
    DialogHeader,
    DialogTitle,
    DialogTrigger
} from "@/components/ui/dialog";
import {Button} from "@/components/ui/button";
import {Label} from "@/components/ui/label";
import {Input, InputProps} from "@/components/ui/input";
import "@/app/globals.css"
import {defaultToast} from "@/hooks/use-toast";
import {ToastAction} from "@/components/ui/toast";


export const deleteDialog = (code : string, refresh: () => void) => {
    const handleConfirm = async () => {
        const success = await deleteApikey(code);
        if (success) {
            refresh();
        } else {
            defaultToast({
                title: "删除ak失败"
            })
        }
    };
    return ActionDialog("删除", "请确认操作", handleConfirm, null, null);
}

export const resetDialog = (code : string, refresh: () => void) => {
    const handleConfirm = async () => {
        const apikey = await resetApikey(code);
        if (apikey) {
            refresh();
            const handleCopy = () => {
                    navigator.clipboard.writeText(apikey).catch(err => {
                        console.error('复制失败:', err);
                    });
            };
            defaultToast({
                title: "重置ak成功",
                description: `ak: ${apikey}`,
                action: <ToastAction altText="复制ak" onClick={handleCopy}>复制ak</ToastAction>,
            })
        } else {
            defaultToast({
                title: "重置ak失败"
            })
        }
    };
    return ActionDialog("重置", "请确认操作", handleConfirm, null, null);
}


export const certifyDialog = (code : string, origin : string, refresh: () => void) => {
    const [certify, setCertify] = useState(origin);

    const handleChange = (e:ChangeEvent<HTMLInputElement>) => {
        setCertify(e.target.value);
    };

    const handleConfirm = async () => {
        if(origin === certify) {
            return;
        }
        const success = await updateCertify(code, certify);
        if (success) {
            refresh();
            defaultToast({
                title: "安全认证成功"
            })
        } else {
            defaultToast({
                title: "安全认证失败"
            })
        }
    };

    const inputProps = {
        id: "certify",
        value: certify,
        onChange: handleChange,
        className: "col-span-3"
    };
    return ActionDialog("安全认证", "请输入安全认证码", handleConfirm, "安全认证码", inputProps);
}

export const quotaDialog = (code : string, origin : number, fetchData: () => void) => {
    const [quota, setQuota] = useState(origin);

    const handleChange = (e:ChangeEvent<HTMLInputElement>) => {
        const value = e.target.value;
        const numberValue = parseFloat(value);

        const isValid = /^\d+(\.\d{1,2})?$/.test(value);

        if (isValid && !isNaN(numberValue)) {
            setQuota(numberValue);
        }
    };

    const handleConfirm = async () => {
        if(origin === quota) {
            return;
        }
        const success = await updateQuota(code, quota);
        if (success) {
            fetchData();
            defaultToast({
                title: "已提交申请"
            })
        } else {
            defaultToast({
                title: "申请修改配额失败"
            })
        }
    };

    const inputProps = {
        id: "quota",
        value: quota,
        onChange: handleChange,
        className: "col-span-3"
    };
    return ActionDialog("修改额度", "请输入每月额度", handleConfirm, "每月额度", inputProps);
}

const ActionDialog = (label : string, description : string, handleConfirm : () => void, inputLabel : string | null, inputProps : InputProps | null) => {
    return (
        <div>
            <Dialog>
                <DialogTrigger asChild>
                    <Button variant="outline">
                        {label}
                    </Button>
                </DialogTrigger>
                <DialogContent className="sm:max-w-[425px] bg-white">
                    <DialogHeader>
                        <DialogTitle>{label}</DialogTitle>
                        <DialogDescription>
                            {description}
                        </DialogDescription>
                    </DialogHeader>
                    {
                        inputLabel && <div className="grid gap-4 py-4">
                            <div className="grid grid-cols-4 items-center gap-4">
                                <Label htmlFor="name" className="text-right">
                                    {label}
                                </Label>
                                {inputProps && <Input {...inputProps} />}
                            </div>
                        </div>
                    }
                    <DialogFooter>
                        <DialogClose asChild>
                            <Button type="submit" onClick={handleConfirm}>提交</Button>
                        </DialogClose>
                    </DialogFooter>
                </DialogContent>
            </Dialog>
        </div>
    );
}
