import type {Metadata} from "next";
import localFont from "next/font/local";
import "./globals.css";
import AuthProvider from "@/components/auth/auth-provider";
import {ThemeProvider} from "next-themes";
import {Toaster} from "@/components/ui/toaster";
import React from "react";

const geistSans = localFont({
  src: "./fonts/GeistVF.woff",
  variable: "--font-geist-sans",
  weight: "100 900",
});
const geistMono = localFont({
  src: "./fonts/GeistMonoVF.woff",
  variable: "--font-geist-mono",
  weight: "100 900",
});

export const metadata: Metadata = {
  title: "Bella-Openapi",
  description: "Bella-Openapi",
};

export default function RootLayout({
                                       children,
                                   }: Readonly<{
    children: React.ReactNode;
}>) {
    return (
        <html lang="en">
        <body className={`${geistSans.variable} ${geistMono.variable} antialiased`}>
        <AuthProvider>
            <div>
                <ThemeProvider
                    attribute='class'
                    defaultTheme='dark'
                    enableSystem
                    disableTransitionOnChange
                >
                    <div>
                        <main>{children}</main>
                        <Toaster/>
                    </div>
                </ThemeProvider>
                <Toaster/>
            </div>
        </AuthProvider>
        </body>
        </html>
    );
}
