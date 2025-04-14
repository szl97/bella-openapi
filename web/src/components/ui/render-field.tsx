import React from 'react';
import { Input } from "@/components/ui/input";
import { Textarea } from "@/components/ui/textarea";
import { Switch } from "@/components/ui/switch";
import { Label } from "@/components/ui/label";
import { Card, CardContent } from "@/components/ui/card";
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select";
import { TypeSchema } from "@/lib/types/openapi";

export const renderField = (schema: TypeSchema, value: any, onChange: (path: string, value: any) => void, path: string = '') => {
    const commonProps = {
        className: "w-full p-2 border rounded focus:ring-2 focus:ring-blue-500",
        id: path,
    };

    switch (schema.valueType) {
        case 'enum':
            return (
                <div>
                    <Select
                        value={value || ''}
                        onValueChange={(selectedValue) => onChange(path, selectedValue)}
                    >
                        <SelectTrigger className="w-full">
                            <SelectValue placeholder={`选择 ${schema.name}`} />
                        </SelectTrigger>
                        <SelectContent>
                            {schema.selections?.map((option) => (
                                <SelectItem key={option} value={option}>
                                    {option}
                                </SelectItem>
                            ))}
                        </SelectContent>
                    </Select>
                </div>
            );
        case 'string':
            return (
                <Input
                    {...commonProps}
                    value={value || ''}
                    onChange={(e) => onChange(path, e.target.value)}
                    placeholder={`输入 ${schema.name}`}
                />
            );
        case 'number':
            return (
                <Input
                    {...commonProps}
                    type="number"
                    value={value || ''}
                    onChange={(e) => onChange(path, parseFloat(e.target.value))}
                    placeholder={`输入 ${schema.name}`}
                />
            );
        case 'bool':
            return (
                <div className="flex items-center">
                    <Switch
                        id={path}
                        checked={value || false}
                        onCheckedChange={(checked) => onChange(path, checked)}
                        className="data-[state=checked]:bg-blue-500 data-[state=unchecked]:bg-gray-200"
                    />
                </div>
            );
        case 'array':
            return (
                <div>
                    <Textarea
                        {...commonProps}
                        value={Array.isArray(value) ? value.join(', ') : ''}
                        onChange={(e) => onChange(path, e.target.value.split(',').map(item => item.trim()))}
                        placeholder={`输入 ${schema.name} (用逗号分隔)`}
                        rows={3}
                    />
                </div>
            );
        case 'object':
            return (
                <Card className="bg-gradient-to-r from-blue-50 to-purple-50 text-gray-800 shadow-sm rounded-md">
                    <CardContent className="p-4">
                        {schema.child?.params.map((param, index) => (
                            <div key={param.code} className={index !== 0 ? "mt-4" : ""}>
                                {renderField(
                                    param,
                                    value?.[param.code],
                                    (nestedPath, nestedValue) => {
                                        const newValue = { ...value, [param.code]: nestedValue };
                                        onChange(path, newValue);
                                    },
                                    `${path}${path ? '.' : ''}${param.code}`
                                )}
                            </div>
                        ))}
                    </CardContent>
                </Card>
            );
        case 'map':
            return (
                <div>
                    <Textarea
                        {...commonProps}
                        value={value ? JSON.stringify(value, null, 2) : ''}
                        onChange={(e) => {
                            try {
                                onChange(path, JSON.parse(e.target.value));
                            } catch (error) {
                                console.error('无效的 JSON 输入', error);
                            }
                        }}
                        placeholder={`输入 ${schema.name} 的 JSON`}
                        rows={5}
                    />
                </div>
            );
        default:
            return (
                <Input {...commonProps} value={value || ''} onChange={(e) => onChange(path, e.target.value)} />
            );
    }
};
