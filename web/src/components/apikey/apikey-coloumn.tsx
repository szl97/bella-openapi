import { ColumnDef } from "@tanstack/react-table";
import { ApikeyInfo } from "@/types/openapi";
import React from "react";
import { certifyDialog, deleteDialog, quotaDialog, resetDialog } from "./apikey-dialog";
import "@/app/globals.css"

export const ApikeyColumns = (refresh: () => void): ColumnDef<ApikeyInfo>[] => [
    {
        accessorKey: "akDisplay",
        header: () => <div className="text-center w-1/8">ak</div>,
        cell: ({ row }) => <div className="text-center w-1/8">{row.getValue("akDisplay")}</div>,
    },
    {
        accessorKey: "name",
        header: () => <div className="text-center w-1/8">名称</div>,
        cell: ({ row }) => <div className="text-center w-1/8">{row.getValue("name")}</div>,
    },
    {
        accessorKey: "serviceId",
        header: () => <div className="text-center w-1/8">服务名</div>,
        cell: ({ row }) => <div className="text-center w-1/8">{row.getValue("serviceId")}</div>,
    },
    {
        accessorKey: "roleCode",
        header: () => <div className="text-center w-1/8">权限等级</div>,
        cell: ({ row }) => <div className="text-center w-1/8">{row.getValue("roleCode")}</div>,
    },
    {
        accessorKey: "safetyLevel",
        header: () => <div className="text-center w-1/8">安全级别</div>,
        cell: ({ row }) => <div className="text-center w-1/8">{row.getValue("safetyLevel")}</div>,
    },
    {
        accessorKey: "monthQuota",
        header: () => <div className="text-center w-1/8">每月额度</div>,
        cell: ({ row }) => {
            const amount = parseFloat(row.getValue("monthQuota"));
            const formatted = new Intl.NumberFormat("zh-CN", {
                style: "currency",
                currency: "CNY",
            }).format(amount);
            return <div className="text-center font-medium w-1/8">{formatted}</div>;
        },
    },
    {
        accessorKey: "remark",
        header: () => <div className="text-center w-1/2">备注</div>,
        cell: ({ row }) => {
            const remark = row.original.remark || '/';
            return (
                <div className="relative group">
                    <div className="truncate w-full">{remark}</div>
                    <div className={`absolute hidden group-hover:block bg-gray-700 text-white text-xs rounded p-2 z-10 whitespace-normal break-words ${remark ? 'w-64' : 'w-32'}`}>
                        {remark}
                    </div>
                </div>
            );
        },
    },
    {
        id: "actions",
        header: () => <div className="text-center w-1/4">操作</div>,
        cell: ({ row }) => {
            const apikey = row.original;
            return (
                <div className="grid place-items-center w-1/4">
                    <div className="flex space-x-3">
                        <div className="flex flex-col space-y-1">
                            {deleteDialog(apikey.code, refresh)}
                            {resetDialog(apikey.code, refresh)}
                        </div>
                        <div className="flex flex-col space-y-1">
                            {certifyDialog(apikey.code, "", refresh)}
                            {quotaDialog(apikey.code, apikey.monthQuota, refresh)}
                        </div>
                    </div>
                </div>
            );
        },
    },
];
