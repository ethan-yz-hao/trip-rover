import {useState} from 'react';
import {Input} from '@chakra-ui/react';

interface Props {
    startTime: string | null;
    handleDateChange: (date: string) => void; // handleDateChange now takes a string
}

const CustomDatePicker = ({startTime, handleDateChange}: Props) => {
    const toLocalDateTime = (isoStr: string | null) => {
        if (!isoStr) return '';
        const date = new Date(isoStr);
        const year = date.getFullYear();
        const month = date.getMonth() + 1;
        const day = date.getDate();
        const hours = date.getHours();
        const minutes = date.getMinutes();
        const monthStr = month < 10 ? `0${month}` : `${month}`;
        const dayStr = day < 10 ? `0${day}` : `${day}`;
        const hoursStr = hours < 10 ? `0${hours}` : `${hours}`;
        const minutesStr = minutes < 10 ? `0${minutes}` : `${minutes}`;
        return `${year}-${monthStr}-${dayStr}T${hoursStr}:${minutesStr}`;
    };

    const [dateTime, setDateTime] = useState(toLocalDateTime(startTime));

    const handleChange = (event: React.ChangeEvent<HTMLInputElement>) => {
        setDateTime(event.target.value);
        handleDateChange(new Date(event.target.value).toISOString());
    };

    return (
        <Input
            type="datetime-local"
            size="md"
            width="200px"
            value={dateTime}
            onChange={handleChange}
            placeholder="Select date and time"
            min={new Date().toISOString().slice(0, 16)}
        />
    );
};

export default CustomDatePicker;
