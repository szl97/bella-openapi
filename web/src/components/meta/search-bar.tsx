import { useState } from 'react';
import { Input } from '@/components/ui/input';
import { Button } from '@/components/ui/button';
import { Search } from 'lucide-react';

interface SearchBarProps {
    onSearch: (term: string) => void;
}

export function SearchBar({ onSearch }: SearchBarProps) {
    const [searchTerm, setSearchTerm] = useState('');

    const handleSearch = () => {
        onSearch(searchTerm);
    };

    return (
        <div className="flex space-x-2 mb-6">
            <Input
                type="text"
                placeholder="搜索模型..."
                value={searchTerm}
                onChange={(e) => setSearchTerm(e.target.value)}
                onKeyPress={(e) => e.key === 'Enter' && handleSearch()}
                className="flex-grow bg-white dark:bg-gray-800 border-gray-300 dark:border-gray-600 focus:border-gray-400 focus:ring-1 focus:ring-gray-100 dark:focus:ring-gray-700"
            />
            <Button onClick={handleSearch} className="bg-gray-500 hover:bg-gray-600 dark:bg-gray-600 dark:hover:bg-gray-700 text-white">
                <Search className="mr-2 h-4 w-4" /> 搜索
            </Button>
        </div>
    );
}
