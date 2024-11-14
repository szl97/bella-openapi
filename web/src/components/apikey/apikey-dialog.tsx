import React, {ChangeEvent, useState} from 'react'
import {deleteApikey, resetApikey, updateCertify, updateQuota, rename} from "@/lib/api/apikey"
import {
    Dialog,
    DialogContent,
    DialogDescription,
    DialogFooter,
    DialogHeader,
    DialogTitle,
    DialogTrigger,
} from "@/components/ui/dialog"
import {Button} from "@/components/ui/button"
import {Label} from "@/components/ui/label"
import {Input} from "@/components/ui/input"
import {useToast} from "@/hooks/use-toast"
import {ToastAction} from "@/components/ui/toast"
import { Tooltip, TooltipContent, TooltipProvider, TooltipTrigger } from "@/components/ui/tooltip"
import {Trash2, RotateCcw, SquarePen} from 'lucide-react'


interface ActionDialogProps {
    label: string
    description: string
    onConfirm: () => Promise<void>
    inputLabel?: string
    inputProps?: React.InputHTMLAttributes<HTMLInputElement>
    icon: React.ReactNode
    isIcon: boolean
    isOpen: boolean
    onClose: () => void
}

const ActionDialog: React.FC<ActionDialogProps> = ({ label, description, onConfirm, inputLabel, inputProps,
                                                       icon, isIcon , isOpen, onClose}) => {
    const [isLoading, setIsLoading] = useState(false)

    const handleConfirm = async () => {
        setIsLoading(true)
        await onConfirm()
        setIsLoading(false)
        onClose()
    }

    return (
        <Dialog open={isOpen} onOpenChange={onClose}>
            <DialogTrigger asChild>
                {isIcon ? (
                    <Button
                        variant="ghost"
                        size="icon"
                        className="p-0 focus:ring-0"
                    >
                        <TooltipProvider>
                            <Tooltip>
                                <TooltipTrigger asChild>
                                    <div>
                                        {icon}
                                        <span className="sr-only">{label}</span>
                                    </div>
                                </TooltipTrigger>
                                <TooltipContent>
                                    <p>{label}</p>
                                </TooltipContent>
                            </Tooltip>
                        </TooltipProvider>
                    </Button>
                ) : (
                    <Button variant="outline" size="sm" className="w-full sm:w-auto">
                        {icon}
                        <span className="sr-only sm:not-sr-only sm:ml-2">{label}</span>
                    </Button>
                )}
            </DialogTrigger>
            <DialogContent className="sm:max-w-[425px] bg-white">
                <DialogHeader>
                    <DialogTitle className="flex items-center gap-2 text-gray-900">
                        {icon}
                        {label}
                    </DialogTitle>
                    <DialogDescription className="text-gray-600">{description}</DialogDescription>
                </DialogHeader>
                {inputLabel && (
                    <div className="grid gap-4 py-4">
                        <div className="grid grid-cols-4 items-center gap-4">
                            <Label htmlFor={inputProps?.id} className="text-right text-gray-700">{inputLabel}</Label>
                            {inputProps && <Input {...inputProps} className="col-span-3"/>}
                        </div>
                    </div>
                )}
                <DialogFooter>
                    <Button type="submit" onClick={handleConfirm} disabled={isLoading}>
                        {isLoading ? "处理中..." : "确认"}
                    </Button>
                </DialogFooter>
            </DialogContent>
        </Dialog>
    )
}

export const DeleteDialog: React.FC<{ code: string; refresh: () => void}> = ({ code, refresh }) => {
    const { toast } = useToast()
    const [isOpen, setIsOpen] = useState(false)

    const handleConfirm = async () => {
        const success = await deleteApikey(code)
        if (success) {
            refresh()
            toast({ title: "API Key 已删除", description: "API Key 已成功删除。" })
        } else {
            toast({ title: "删除失败", description: "无法删除 API Key，请稍后重试。", variant: "destructive" })
        }
    }

    return (
        <ActionDialog
            label="删除"
            description="确定要删除此 API Key 吗？此操作无法撤销。"
            onConfirm={handleConfirm}
            icon={<Trash2 className="h-4 w-4" />}
            isIcon={true}
            isOpen={isOpen}
            onClose={() => setIsOpen(false)}
        />
    )
}

export const ResetDialog: React.FC<{ code: string; refresh: () => void }> = ({ code, refresh }) => {
    const { toast } = useToast()
    const [isOpen, setIsOpen] = useState(false)

    const handleConfirm = async () => {
        const apikey = await resetApikey(code)
        if (apikey) {
            refresh()
            const handleCopy = () => {
                navigator.clipboard.writeText(apikey).catch(err => {
                    console.error('复制失败:', err)
                })
            }
            toast({
                title: "API Key 已重置",
                description: `新的 API Key: ${apikey}`,
                action: <ToastAction altText="复制 API Key" onClick={handleCopy}>复制</ToastAction>,
            })
        } else {
            toast({ title: "重置失败", description: "无法重置 API Key，请稍后重试。", variant: "destructive" })
        }
    }

    return (
        <ActionDialog
            label="重置"
            description="确定要重置此 API Key 吗？重置后，当前的 Key 将失效。"
            onConfirm={handleConfirm}
            icon={<RotateCcw className="h-4 w-4" />}
            isIcon={true}
            isOpen={isOpen}
            onClose={() => setIsOpen(false)}
        />
    )
}

export const CertifyDialog: React.FC<{ code: string; refresh: () => void; isOpen: boolean; onClose: () => void }> = ({ code, refresh, isOpen, onClose }) => {
    const [certify, setCertify] = useState("")
    const { toast } = useToast()

    const handleChange = (e: ChangeEvent<HTMLInputElement>) => {
        setCertify(e.target.value)
    }

    const handleConfirm = async () => {
        if (certify === "") return
        const success = await updateCertify(code, certify)
        if (success) {
            refresh()
            toast({ title: "安全认证成功", description: "API Key 的安全认证已更新。" })
        } else {
            toast({ title: "安全认证失败", description: "无法更新安全认证，请稍后重试。", variant: "destructive" })
        }
    }

    return (
        <ActionDialog
            label="安全认证"
            description="请输入新的安全认证码。"
            onConfirm={handleConfirm}
            inputLabel="安全认证码"
            inputProps={{
                id: "certify",
                value: certify,
                onChange: handleChange,
                placeholder: "输入新的安全认证码",
            }}
            icon={<SquarePen className="h-4 w-4" />}
            isIcon={true}
            isOpen={isOpen}
            onClose={onClose}
        />
    )
}

export const QuotaDialog: React.FC<{ code: string; origin: number; refresh: () => void; isOpen: boolean; onClose: () => void }> = ({ code, origin, refresh, isOpen, onClose }) => {
    const [quota, setQuota] = useState(origin)
    const { toast } = useToast()

    const handleChange = (e: ChangeEvent<HTMLInputElement>) => {
        const value = e.target.value
        if (!value) {
            setQuota(0)
            return
        }
        const numberValue = parseFloat(value)
        const isValid = /^\d+(\.\d{1,2})?$/.test(value)
        if (isValid && !isNaN(numberValue)) {
            setQuota(numberValue)
        }
    }

    const handleConfirm = async () => {
        if (origin === quota) return
        const success = await updateQuota(code, quota)
        if (success) {
            refresh()
            toast({ title: "额度修改申请已提交", description: "您的额度修改申请已成功提交，请等待审核。" })
        } else {
            setQuota(origin)
            toast({ title: "修改失败", description: "请稍后重试。", variant: "destructive" })
        }
    }

    return (
        <ActionDialog
            label="修改额度"
            description="请输入新的每月额度。"
            onConfirm={handleConfirm}
            inputLabel="每月额度"
            inputProps={{
                id: "quota",
                value: quota,
                onChange: handleChange,
                type: "number",
                min: 0,
                step: 0.01,
            }}
            icon={<SquarePen className="h-4 w-4" />}
            isIcon={true}
            isOpen={isOpen}
            onClose={onClose}
        />
    )
}

export const RenameDialog: React.FC<{ code: string; origin: string; refresh: () => void; isOpen: boolean; onClose: () => void }> = ({ code, origin, refresh, isOpen, onClose }) => {
    const [name, setName] = useState(origin)
    const { toast } = useToast()

    const handleChange = (e: ChangeEvent<HTMLInputElement>) => {
        const value = e.target.value
        if (!value) {
            setName("")
        } else {
            setName(value)
        }
    }

    const handleConfirm = async () => {
        if (origin === name) return
        const success = await rename(code, name)
        if (success) {
            refresh()
            toast({ title: "修改成功", description: "apikey名称修改为:" + name })
        } else {
            setName(origin)
            toast({ title: "修改失败", description: "无法提交额度修改申请，请稍后重试。", variant: "destructive" })
        }
    }

    return (
        <ActionDialog
            label="修改额度"
            description="请输入新的每月额度。"
            onConfirm={handleConfirm}
            inputLabel="名称"
            inputProps={{
                id: "name",
                value: name,
                onChange: handleChange,
                placeholder: "输入名称",
            }}
            icon={<SquarePen className="h-4 w-4" />}
            isIcon={true}
            isOpen={isOpen}
            onClose={onClose}
        />
    )
}
