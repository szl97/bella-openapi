'use client';
import React, { createContext, useState, useEffect, useContext } from 'react';
import { getUserInfo } from '@/lib/api/userInfo';

export interface UserInfo {
    userId: number;
    userName: string;
    image?: string;
}

interface UserContextType {
    userInfo: UserInfo | null;
    setUserInfo: React.Dispatch<React.SetStateAction<UserInfo | null>>;
    isLoading: boolean;
    error: Error | null;
}

const UserContext = createContext<UserContextType | undefined>(undefined);

export const UserProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
    const [userInfo, setUserInfo] = useState<UserInfo | null>(null);
    const [isLoading, setIsLoading] = useState(true);
    const [error, setError] = useState<Error | null>(null);

    useEffect(() => {
        const fetchUserInfo = async () => {
            try {
                const info = await getUserInfo();
                setUserInfo(info);
            } catch (err) {
                setError(err instanceof Error ? err : new Error('An error occurred'));
            } finally {
                setIsLoading(false);
            }
        };

        fetchUserInfo();
    }, []);

    return (
        <UserContext.Provider value={{ userInfo, setUserInfo, isLoading, error }}>
            {children}
        </UserContext.Provider>
    );
};

export const useUser = () => {
    const context = useContext(UserContext);
    if (context === undefined) {
        throw new Error('useUser must be used within a UserProvider');
    }
    return context;
};
