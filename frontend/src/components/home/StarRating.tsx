import {Box, Flex, Icon} from '@chakra-ui/react';

// Define SVG paths for the star icons
const FILLED_STAR_PATH = "M12 .587l3.668 7.431 8.332 1.21-6.001 5.852 1.417 8.265L12 18.896l-7.416 3.899 1.416-8.265-6-5.852 8.332-1.21L12 .587z";
// Star component accepting a filling prop to determine its appearance
const Star = ({filling = 1}) => {
    if (filling === 1) {
        return <Icon viewBox="0 0 24 24" fill="yellow.300">
            <path d={FILLED_STAR_PATH}/>
        </Icon>;
    } else if (filling === 0) {
        return <Icon viewBox="0 0 24 24" fill="gray.400">
            <path d={FILLED_STAR_PATH}/>
        </Icon>;
    } else {
        // Render partially filled star using CSS for simplicity
        return (
            <Box position="relative" display="inline-block">
                <Box overflow="hidden" width={`${filling * 100}%`} position="absolute" style={{zIndex: 3}}>
                    <Icon viewBox="0 0 24 24" fill="yellow.300">
                        <path d={FILLED_STAR_PATH}/>
                    </Icon>
                </Box>
                <Box position="absolute">
                    <Icon viewBox="0 0 24 24" fill="gray.400">
                        <path d={FILLED_STAR_PATH}/>
                    </Icon>
                </Box>
                <Icon viewBox="0 0 24 24" fill="currentColor" visibility="hidden"><path d={FILLED_STAR_PATH} /></Icon>
            </Box>
        );
    }
};

// StarRating component that renders a 5-star rating based on a given value
export const StarRating = ({value}: { value: number }) => {
    return (
        <Flex>
            {Array.from({length: 5}).map((_, index) => {
                const starSerialNumber = index + 1;
                let filling = 0;

                if (starSerialNumber <= Math.floor(value)) {
                    filling = 1;
                } else if (starSerialNumber > Math.ceil(value)) {
                    filling = 0;
                } else {
                    filling = value - index;
                }

                return (
                    <Box key={index} className="star" px="0.5">
                        <Star filling={filling}/>
                    </Box>
                );
            })}
        </Flex>
    );
};
