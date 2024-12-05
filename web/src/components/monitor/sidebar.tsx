import { useState, useEffect } from 'react';
import { CategoryTree } from '@/lib/types/openapi';
import { getAllCategoryTrees } from '@/lib/api/meta';

interface SidebarProps {
    onCategorySelect: (category: string) => void;
    defaultCategory?: string;
}

export function Sidebar({ onCategorySelect, defaultCategory }: SidebarProps) {
    const [categoryTrees, setCategoryTrees] = useState<CategoryTree[]>([]);
    const [activeCategory, setActiveCategory] = useState<string | null>(null);

    useEffect(() => {
        async function fetchData() {
            const trees = await getAllCategoryTrees();
            setCategoryTrees(trees);
            if (!activeCategory && trees.length > 0) {
                const firstEndpoint = trees[0].endpoints?.[0]?.endpoint;
                if (firstEndpoint) {
                    handleCategoryClick(firstEndpoint);
                }
            }
        }
        fetchData();
    }, []);

    useEffect(() => {
        if (defaultCategory && !activeCategory) {
            setActiveCategory(defaultCategory);
        }
    }, [defaultCategory]);

    const handleCategoryClick = (category: string) => {
        setActiveCategory(category);
        onCategorySelect(category);
    };

    const renderCategoryTree = (tree: CategoryTree) => (
        <div key={tree.categoryCode} className="mb-6">
            <h2 className="font-semibold text-lg mb-3 text-gray-700">{tree.categoryName}</h2>
            {tree.endpoints && tree.endpoints.map(endpoint => (
                <div
                    key={endpoint.endpointCode}
                    className={`cursor-pointer p-2 rounded-lg transition-colors duration-200 ${
                        activeCategory === endpoint.endpoint
                            ? 'bg-blue-100 text-blue-700'
                            : 'hover:bg-gray-100 text-gray-600'
                    }`}
                    onClick={() => handleCategoryClick(endpoint.endpoint)}
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
                                activeCategory === endpoint.endpoint
                                    ? 'bg-blue-100 text-blue-700'
                                    : 'hover:bg-gray-100 text-gray-600'
                            }`}
                            onClick={() => handleCategoryClick(endpoint.endpoint)}
                        >
                            {endpoint.endpointName}
                        </div>
                    ))}
                </div>
            ))}
        </div>
    );

    return (
        <div className="w-64 bg-white h-screen p-6 border-r border-gray-200">
            <h2 className="font-semibold text-lg mb-6 text-gray-700">能力点列表</h2>
            {categoryTrees.map(renderCategoryTree)}
        </div>
    );
}
