import { useState } from 'react';
import { CategoryTree } from '@/lib/types/openapi';

interface SidebarProps {
    categoryTrees: CategoryTree[];
    onEndpointSelect: (endpoint: string) => void;
    defaultEndpoint: string;
}

export function Sidebar({categoryTrees, onEndpointSelect, defaultEndpoint }: SidebarProps) {
    const [activeEndpoint, setActiveEndpoint] = useState<string | null>(null);
    if(activeEndpoint == null) {
        setActiveEndpoint(defaultEndpoint)
    }
    const handleEndpointClick = (endpoint: string) => {
        setActiveEndpoint(endpoint);
        onEndpointSelect(endpoint);
    };

    const renderCategoryTree = (tree: CategoryTree) => (
        <div key={tree.categoryCode} className="mb-6">
            <h2 className="font-semibold text-lg mb-3 text-gray-700">{tree.categoryName}</h2>
            {tree.endpoints && tree.endpoints.map(endpoint => (
                <div
                    key={endpoint.endpointCode}
                    className={`cursor-pointer p-2 rounded-lg transition-colors duration-200 ${
                        activeEndpoint === endpoint.endpoint
                            ? 'bg-blue-100 text-blue-700'
                            : 'hover:bg-gray-100 text-gray-600'
                    }`}
                    onClick={() => handleEndpointClick(endpoint.endpoint)}
                >
                    {endpoint.endpointName}
                </div>
            ))}
            {tree.children && tree.children.map(child => (
                <div key={child.categoryCode} className="mt-4">
                    <h3 className="font-medium text-base mb-2 text-gray-600">{child.categoryName}</h3>
                    {child.endpoints && child.endpoints.map(endpoint => (
                        <div
                            key={endpoint.endpointCode}
                            className={`cursor-pointer p-2 rounded-lg transition-colors duration-200 ${
                                activeEndpoint === endpoint.endpoint
                                    ? 'bg-blue-100 text-blue-700'
                                    : 'hover:bg-gray-100 text-gray-600'
                            }`}
                            onClick={() => handleEndpointClick(endpoint.endpoint)}
                        >
                            {endpoint.endpointName}
                        </div>
                    ))}
                </div>
            ))}
        </div>
    );

    return (
        <aside className="w-64 bg-white shadow-md p-6 overflow-y-auto h-screen">
            {categoryTrees.map(renderCategoryTree)}
        </aside>
    );
}
