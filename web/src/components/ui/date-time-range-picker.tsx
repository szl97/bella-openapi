'use client';

import * as React from "react";
import { format } from "date-fns";
import { Calendar as CalendarIcon } from "lucide-react";
import { DateRange } from "react-day-picker";
import { cn } from "@/lib/utils";
import { Button } from "@/components/ui/button";
import { Calendar } from "@/components/ui/calendar";
import {
  Popover,
  PopoverContent,
  PopoverTrigger,
} from "@/components/ui/popover";
import { Input } from "./input";

interface DateTimeRangePickerProps {
  startDate: Date;
  endDate: Date;
  onChange: (startDate: Date, endDate: Date) => void;
  maxDate?: Date;
  minDate?: Date;
}

export function DateTimeRangePicker({
  startDate,
  endDate,
  onChange,
  maxDate,
  minDate,
}: DateTimeRangePickerProps) {
  const [selectedRange, setSelectedRange] = React.useState<DateRange>({
    from: startDate,
    to: endDate,
  });

  const handleDateRangeSelect = (range: DateRange | undefined) => {
    if (range?.from) {
      const newStartDate = new Date(range.from);
      newStartDate.setHours(startDate.getHours());
      newStartDate.setMinutes(startDate.getMinutes());

      let newEndDate = endDate;
      if (range.to) {
        newEndDate = new Date(range.to);
        newEndDate.setHours(endDate.getHours());
        newEndDate.setMinutes(endDate.getMinutes());
      }

      setSelectedRange({ from: newStartDate, to: newEndDate });
      onChange(newStartDate, newEndDate);
    }
  };

  const handleStartTimeChange = (event: React.ChangeEvent<HTMLInputElement>) => {
    const timeString = event.target.value;
    if (timeString && selectedRange.from) {
      const [hours, minutes] = timeString.split(':').map(Number);
      const newStartDate = new Date(selectedRange.from);
      newStartDate.setHours(hours);
      newStartDate.setMinutes(minutes);
      
      const newEndDate = selectedRange.to || endDate;
      if (newStartDate <= newEndDate) {
        setSelectedRange({ from: newStartDate, to: newEndDate });
        onChange(newStartDate, newEndDate);
      }
    }
  };

  const handleEndTimeChange = (event: React.ChangeEvent<HTMLInputElement>) => {
    const timeString = event.target.value;
    const baseDate = selectedRange.to || selectedRange.from;
    if (timeString && baseDate) {
      const [hours, minutes] = timeString.split(':').map(Number);
      const newEndDate = new Date(baseDate);
      newEndDate.setHours(hours);
      newEndDate.setMinutes(minutes);
      
      if (selectedRange.from && newEndDate >= selectedRange.from) {
        setSelectedRange({ ...selectedRange, to: newEndDate });
        onChange(selectedRange.from, newEndDate);
      }
    }
  };

  return (
    <div className="flex items-center gap-2">
      <Popover>
        <PopoverTrigger asChild>
          <Button
            variant={"outline"}
            className={cn(
              "w-[300px] justify-start text-left font-normal",
              !startDate && "text-muted-foreground"
            )}
          >
            <CalendarIcon className="mr-2 h-4 w-4" />
            {selectedRange?.from ? (
              selectedRange.to ? (
                <>
                  {selectedRange.from ? format(selectedRange.from, "yyyy-MM-dd") : ''} -{" "}
                  {selectedRange.to ? format(selectedRange.to, "yyyy-MM-dd") : ''}
                </>
              ) : (
                selectedRange.from ? format(selectedRange.from, "yyyy-MM-dd") : ''
              )
            ) : (
              <span>Pick a date range</span>
            )}
          </Button>
        </PopoverTrigger>
        <PopoverContent className="w-auto p-0" align="start">
          <Calendar
            initialFocus
            mode="range"
            defaultMonth={startDate}
            selected={selectedRange}
            onSelect={handleDateRangeSelect}
            numberOfMonths={2}
          />
        </PopoverContent>
      </Popover>
      <Input
        type="time"
        value={selectedRange.from ? format(selectedRange.from, "HH:mm") : format(startDate, "HH:mm")}
        onChange={handleStartTimeChange}
        className="w-[120px]"
        step="60"
      />
      <span>至</span>
      <Input
        type="time"
        value={selectedRange.to ? format(selectedRange.to, "HH:mm") : format(endDate, "HH:mm")}
        onChange={handleEndTimeChange}
        className="w-[120px]"
        step="60"
      />
    </div>
  );
}
