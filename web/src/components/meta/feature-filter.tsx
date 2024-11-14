import { MetadataFeature } from '@/lib/types/openapi';

interface FeatureFilterProps {
    features: MetadataFeature[];
    onSelect: (selectedFeatures: string[]) => void;
    selectedFeatures: string[];
}

export function FeatureFilter({ features, onSelect, selectedFeatures }: FeatureFilterProps) {
    const handleFeatureToggle = (featureCode: string) => {
        const updatedFeatures = selectedFeatures.includes(featureCode)
            ? selectedFeatures.filter(f => f !== featureCode)
            : [...selectedFeatures, featureCode];

        onSelect(updatedFeatures);
    };

    return (
        <div className="mb-6">
            <div className="flex flex-wrap gap-2">
                {features && features.map(feature => (
                    <button
                        key={feature.code}
                        onClick={() => handleFeatureToggle(feature.code)}
                        className={`
                            px-3 py-1 rounded-full text-sm font-medium
                            transition-all duration-200 ease-in-out
                            ${selectedFeatures.includes(feature.code)
                            ? 'bg-gray-700 text-white border-2 border-gray-500'
                            : 'bg-gray-200 text-gray-700 border-2 border-transparent hover:bg-gray-300'}
                        `}
                    >
                        {feature.name}
                    </button>
                ))}
            </div>
        </div>
    );
}
