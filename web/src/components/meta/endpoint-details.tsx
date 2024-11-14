import { useState, useEffect } from 'react';
import { getEndpointDetails } from '@/lib/api/endpoint';
import { EndpointDetails, Model } from '@/lib/types/openapi';
import { ModelCard } from './model-card';
import { SearchBar } from './search-bar';
import { FeatureFilter } from './feature-filter';
import {Loader} from "lucide-react";

interface EndpointDetailsProps {
    endpoint: string | null;
}

export function EndpointDisplay({ endpoint }: EndpointDetailsProps) {
    const [endpointData, setEndpointData] = useState<EndpointDetails | null>(null);
    const [endpointModels, setEndpointModels] = useState<Model[]>([]);
    const [searchTerm, setSearchTerm] = useState('');
    const [selectedFeatures, setSelectedFeatures] = useState<string[]>([]);

    useEffect(() => {
        setSelectedFeatures([]);
        setSearchTerm('');
    }, [endpoint]);

    useEffect(() => {
        async function fetchEndpointModels() {
            if (endpoint) {
                const data = await getEndpointDetails(endpoint, searchTerm, selectedFeatures);
                setEndpointData(data);
                setEndpointModels(data.models);
            } else {
                setEndpointData(null);
                setEndpointModels([]);
            }
        }
        fetchEndpointModels();
    }, [searchTerm, selectedFeatures]);

    if (!endpoint || !endpointData) {
        return (
            <div className="flex justify-center items-center p-4">
                <Loader className="h-8 w-8 animate-spin" />
            </div>
        )
    }

    return (
        <div className="p-4">
            <div className="mb-4">
                <SearchBar onSearch={setSearchTerm} />
            </div>
            <div className="mb-4">
                <FeatureFilter features={endpointData.features} onSelect={setSelectedFeatures} selectedFeatures={selectedFeatures} />
            </div>
            {endpointModels && <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
                {endpointModels.map(model => (
                    <ModelCard key={model.modelName} model={model} />
                ))}
            </div>}
        </div>
    );
}
