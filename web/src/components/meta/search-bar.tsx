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
                className="flex-grow bg-gray-100 border-gray-300 focus:border-gray-500 focus:ring-2 focus:ring-gray-200"
            />
            <Button onClick={handleSearch} className="bg-gray-700 hover:bg-gray-800 text-white">
                <Search className="mr-2 h-4 w-4" /> 搜索
            </Button>
        </div>
    );
}
