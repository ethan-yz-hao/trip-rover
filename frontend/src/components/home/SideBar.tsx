import {useContext, useEffect, useState} from "react";
import {Button, Card, Flex, HStack, Icon, IconButton, Input, List, ListItem, Text, VStack} from "@chakra-ui/react";
import {selectPlacesArray, setPlacesArray} from '@/features/placesArray/placesArraySlice';
import {useAppDispatch, useAppSelector} from '@/hooks/reduxHooks';
import {DragDropContext, Draggable, Droppable, DropResult} from '@hello-pangea/dnd';
import {AddIcon, DeleteIcon, DragHandleIcon, HamburgerIcon} from '@chakra-ui/icons';
import {selectStartTime, setStartTime} from "@/features/startTime/startTimeSlice.ts";
import {formatISO} from "date-fns";
import {selectArrivalTimeArray} from "@/features/arrivalTimeArray/arrivalTimeArraySlice.ts";
import CustomDatePicker from "@/components/home/CustomDatePicker.tsx";
import CustomTimePicker from "@/components/home/CustomTimePicker.tsx";
import {Plan, StoreContext} from "../../context/StoreContext.tsx";
import {useForm} from "react-hook-form";


const SideBar = () => {
    const dispatch = useAppDispatch();
    const placesArray = useAppSelector(selectPlacesArray);
    const startTime = useAppSelector(selectStartTime);
    const arrivalTimeArray = useAppSelector(selectArrivalTimeArray);

    useEffect(() => {
        if (!placesArray) return;
        console.log('placesArray', placesArray);
    }, [placesArray]);

    const handleRemovePlace = (id: string) => {
        const newPlacesArray = placesArray.filter(place => place.id !== id);
        dispatch(setPlacesArray(newPlacesArray));
    };

    const handleDateChange = (date: string | null) => {
        if (!date) return;
        const isoDate = formatISO(date, {representation: 'complete'});
        dispatch(setStartTime(isoDate));
    };


    const onDragEnd = (result: DropResult) => {
        const {destination, source} = result;

        if (!destination) {
            return;
        }

        if (destination.index === source.index) {
            return;
        }

        const newPlacesArray = Array.from(placesArray);
        const [removed] = newPlacesArray.splice(source.index, 1);
        newPlacesArray.splice(destination.index, 0, removed);

        dispatch(setPlacesArray(newPlacesArray));
    };

    const addSeconds = (date: string, seconds: number) => {
        return new Date(new Date(date).setSeconds(new Date(date).getSeconds() + seconds));
    }

    // handle plan CRUD
    const {currentUserId, plans, addPlan, deletePlan, modifyPlan} = useContext(StoreContext);
    const [currentPlan, setCurrentPlan] = useState<Plan | null>(null);

    const {register, handleSubmit, setValue, formState: {errors}} = useForm({
        defaultValues: {
            title: '',
            places: placesArray,
            startTime: startTime
        }
    });

    const onSubmit = (data: { title: string, startTime: string }) => {
        if (!currentUserId) {
            console.log('User not logged in');
            return;
        }
        if (currentPlan) {
            modifyPlan({...currentPlan, title: data.title, places: placesArray, startTime: data.startTime});
        } else {
            addPlan(currentUserId, data.title, placesArray, data.startTime);
        }

    };

    const onPlanLoad = (plan: Plan) => {
        setCurrentPlan(plan);
        setValue('title', plan.title);
        dispatch(setPlacesArray(plan.places));
        dispatch(setStartTime(plan.startTime));
    }

    const onDeletePlan = (plan: Plan) => {
        deletePlan(plan.id);
    }

    const onNewPlan = () => {
        setCurrentPlan(null);
        dispatch(setPlacesArray([]));
        dispatch(setStartTime(formatISO(new Date(), {representation: 'complete'})));
    }

    const [isMenuOpen, setIsMenuOpen] = useState(false);

    return (
        <Flex
            direction="column"
            height={{lg: "calc(100vh - 50px)", base: "none"}}
            maxHeight={{lg: "none", base: "350px"}}
            width="100%"
            pb={2}
        >
            <HStack width="100%" justifyContent="space-between" p={2} boxSizing="border-box">
                <Text textAlign="start" fontSize="3xl" fontWeight="semibold">
                    Trip Plans
                </Text>
                <HStack>
                    <Button onClick={onNewPlan}>
                        <HStack>
                            <Icon as={AddIcon} boxSize={3}/>
                            <Text>New Plan</Text>
                        </HStack>
                    </Button>
                    {currentUserId &&
                        <IconButton aria-label="Dropdown Menu"
                                    onClick={() => setIsMenuOpen(!isMenuOpen)}
                                    icon={<HamburgerIcon/>}/>
                    }
                </HStack>
            </HStack>
            {isMenuOpen &&
                <VStack p={2}>
                    {plans && plans.map(plan => {
                        return (
                            <HStack width="100%" key={plan.id} spacing={2} justifyContent="space-between"
                                    borderRadius={8} p={2}
                                    backgroundColor={currentPlan && currentPlan.id === plan.id ? 'gray.200' : 'white'}
                            >
                                <Text fontWeight="semibold" fontSize={18}>{plan.title}</Text>
                                <HStack>
                                    <Text>{new Date(plan.createdAt).toLocaleString('en-US', {
                                        month: 'short',
                                        day: 'numeric',
                                        hour: 'numeric',
                                        minute: 'numeric',
                                        hour12: true
                                    })}</Text>
                                    <Button onClick={() => onPlanLoad(plan)}>Load</Button>
                                    <IconButton aria-label="Delete Plan"
                                                onClick={() => onDeletePlan(plan)}
                                                icon={<DeleteIcon/>}/>
                                </HStack>
                            </HStack>
                        )
                    })}
                </VStack>
            }
            {currentUserId &&
                <form onSubmit={handleSubmit(onSubmit)}>
                    <VStack spacing={1} p={2}>
                        <HStack width="100%">
                            <Text fontSize={16} fontWeight="semibold">Title: </Text>
                            <Input id="title"
                                   placeholder="Enter trip title"
                                   flexGrow="1"
                                   {...register("title", {required: true})}
                            />
                            {currentPlan ?
                                <Button type="submit" width="100px">Update</Button>
                                : <Button type="submit">Save</Button>
                            }
                        </HStack>
                        {errors.title && <Text color="red.500">This field is required</Text>}
                    </VStack>
                </form>
            }
            <HStack width="full" px={2}>
                <Text fontSize={16} fontWeight="semibold">Start Time:</Text>
                <CustomDatePicker startTime={startTime} handleDateChange={handleDateChange}/>
            </HStack>
            <DragDropContext onDragEnd={onDragEnd}>
                <Droppable droppableId="places">
                    {(provided) => (
                        <List {...provided.droppableProps} ref={provided.innerRef}
                              width="100%"
                              height="100%"
                              overflowY="auto"
                        >
                            {placesArray.map((place, index) => {
                                return (
                                    <Draggable key={place.id} draggableId={place.id} index={index}>
                                        {(provided) => (
                                            <ListItem
                                                ref={provided.innerRef}
                                                {...provided.draggableProps}
                                                {...provided.dragHandleProps}
                                                margin={2}
                                                backgroundColor="white"
                                                padding={2}
                                                borderRadius={8}
                                                as={Card}
                                            >
                                                <HStack spacing={2} justifyContent="space-between">
                                                    <HStack spacing={2}>
                                                        <Icon as={DragHandleIcon} w={4} h={4}/>
                                                        <Text>{place.name}</Text>
                                                    </HStack>
                                                    <IconButton aria-label="Delete Place"
                                                                onClick={() => handleRemovePlace(place.id)}
                                                                icon={<DeleteIcon/>}/>
                                                </HStack>
                                                {arrivalTimeArray[index] &&
                                                    <VStack spacing={2} justifyContent="space-between">
                                                        <HStack width="full"
                                                                justifyContent="space-between">
                                                            <Text>
                                                                Arrival:{' '}
                                                                {new Date(arrivalTimeArray[index]).toLocaleString('en-US', {
                                                                    month: 'short',
                                                                    day: 'numeric',
                                                                    hour: 'numeric',
                                                                    minute: 'numeric',
                                                                    hour12: true
                                                                })}
                                                            </Text>
                                                            <Text>
                                                                Departure:{' '}
                                                                {addSeconds(arrivalTimeArray[index], place.stayTime).toLocaleString('en-US', {
                                                                    month: 'short',
                                                                    day: 'numeric',
                                                                    hour: 'numeric',
                                                                    minute: 'numeric',
                                                                    hour12: true
                                                                })}
                                                            </Text>
                                                        </HStack>
                                                        <HStack width="full">
                                                            <Text>Stay Time:</Text>
                                                            <CustomTimePicker placeId={place.id}/>
                                                        </HStack>
                                                    </VStack>
                                                }
                                            </ListItem>
                                        )}
                                    </Draggable>
                                )
                            })}
                            {provided.placeholder}
                        </List>
                    )}
                </Droppable>
            </DragDropContext>
        </Flex>
    );
};

export default SideBar;
