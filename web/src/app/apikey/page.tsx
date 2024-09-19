"use client"
import React, {useEffect, useState} from "react";
import {DataTable} from "@/components/data-table";
import {getApikeyInfos} from "@/app/api/apikey";
import {ApikeyColumns} from "@/components/apikey/apikey-coloumn";
import {ApikeyInfo} from "@/types/openapi";
import "@/app/globals.css"
import {ClientHeader} from "@/components/client-header";

const ApikeyPage = () => {
    const [page, setPage] = useState(0);
    const [data, setData] = useState<ApikeyInfo[] | null>(null);
    const [hasMore, setHasMore] = useState(true);

    const handleNextPage = () => {
        if (hasMore) {
            setPage(page + 1);
        }
    };

    const handlePreviousPage = () => {
        if (page > 0) {
            setPage(page - 1);
        }
    };

    const refresh = () => {
        getApikeyInfos(page + 1).then(res => {
            setHasMore(res?.has_more || false);
            setData(res?.data || null);
        }).catch(error => {
            console.error('Failed to fetch API keys:', error);
            setData(null);
        });
    }

    useEffect(() => {
        refresh();
    }, [page]);

    const columns = ApikeyColumns(refresh);

    interface ButtonProps {
        onClick: () => void;
        disabled: boolean;
        children: React.ReactNode;
    }

    const Button: React.FC<ButtonProps> = ({ onClick, disabled, children }) => {
        const baseStyles = "px-4 py-2 rounded text-white font-bold";
        const enabledStyles = "bg-black hover:bg-gray-300";
        const disabledStyles = "bg-gray-400 cursor-not-allowed";

        return (
            <button
                onClick={onClick}
                disabled={disabled}
                className={`${baseStyles} ${disabled ? disabledStyles : enabledStyles}`}
            >
                {children}
            </button>
        );
    };

    return (
        <div>
            <ClientHeader title='api key'/>
            {data && <DataTable columns={columns} data={data}/>}
            <div className="flex justify-between mt-4">
                <Button onClick={handlePreviousPage} disabled={page === 0}>
                    上一页
                </Button>
                <span>当前页: {page + 1}</span>
                <Button onClick={handleNextPage} disabled={!hasMore}>
                    下一页
                </Button>
            </div>
        </div>

    );
};

export default ApikeyPage;
