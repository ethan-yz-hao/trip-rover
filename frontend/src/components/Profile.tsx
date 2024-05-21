import { useContext } from "react";
import {  useParams, Navigate } from "react-router-dom";
import { StoreContext } from "../context/StoreContext";
import {
  Box,
  Flex,
  Text,
  Button,
  Image,
  useColorModeValue,
  HStack,
  VStack
} from '@chakra-ui/react';

const Profile = () => {
  let {
    users, plans, currentUserId,
  } = useContext(StoreContext);

  const { userId } = useParams();


  const user = users.find((userId === undefined) ? user => user.id === currentUserId : user => user.id === userId);
  const { signout } = useContext(StoreContext);

  
  const getPlans = () => {
    return plans.filter((plan) => plan.userId === currentUserId);
  }



const printPlans = () => {
  return getPlans().map((plan) => (
    <VStack key={plan.id} p={4} boxShadow="md" borderRadius="lg" spacing={4}>
      <Text fontSize="lg" fontWeight="bold">{plan.title}</Text>
      <Text fontSize="lg" fontWeight="bold">{plan.startTime}</Text>
      {plan.places.map((place, index) => (
        <Box key={index} p={2} boxShadow="base" borderRadius="md">
          {typeof place === 'string' ? place : place.name} 
        </Box>
      ))}
    </VStack>
  ));
}

  const countPlans = () => {
    let plans1 = getPlans();
    return plans1.length;
  }

  return (
    !user ? <Navigate to="/login" /> : <div>
    <VStack spacing={6} align="stretch" p={5} bg={useColorModeValue('gray.50', 'gray.900')} borderRadius="lg" boxShadow="xl">
      <Flex justify="space-between" align="center">
        <HStack spacing={4}>
          <Image src={user.photo} alt="Profile photo" boxSize="100px" borderRadius="full" />
          <VStack align="start">
            <Text fontSize="2xl" fontWeight="bold">{user.name}</Text>
            <Text fontSize="md" color="gray.500">{user.bio}</Text>
            <Button colorScheme="blue" onClick={signout}>Sign Out</Button>
          </VStack>
        </HStack>
      </Flex>
      <HStack justifyContent="space-between">
        <VStack>
          <Text fontSize="xl" fontWeight="bold">{countPlans()}</Text>
          <Text fontSize="sm" color="gray.500">plans</Text>
        </VStack>
      </HStack>
      <Box>
        {printPlans()}
      </Box>
    </VStack>
    </div>
  );
};


export default Profile;
