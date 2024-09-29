// import {useState, useContext, ChangeEvent, FormEvent} from 'react';
// import {useNavigate} from "react-router-dom";
// import { StoreContext } from '../context/StoreContext';
// import {
//     Box,
//     Button,
//     FormControl,
//     FormLabel,
//     Input,
//     VStack,
//     Text,
//     Link,
//     useToast
// } from '@chakra-ui/react';


function Login() {
    // const navigate = useNavigate();
    // const [email, setEmail] = useState<string>('');
    // const [password, setPassword] = useState<string>('');
    // const [error, setError] = useState<string>('');
    // const { login } = useContext(StoreContext);
    // const toast = useToast();
    //
    // function handleEmail(e: ChangeEvent<HTMLInputElement>) {
    //     setEmail(e.target.value);
    // }
    //
    // function handlePassword(e: ChangeEvent<HTMLInputElement>) {
    //     setPassword(e.target.value);
    // }
    //
    // function handleSubmit(e: FormEvent<HTMLFormElement>) {
    //     e.preventDefault();
    //     if (email === '' || password === '') {
    //         setError('Either email or password is missing...');
    //         toast({
    //             title: "Error",
    //             description: "Either email or password is missing...",
    //             status: "error",
    //             duration: 9000,
    //             isClosable: true,
    //         });
    //         return;
    //     }
    //     login(email, password);
    //     setEmail('');
    //     setPassword('');
    //     setError('');
    // }
    //
    // function handleSignup() {
    //     navigate('/signup');
    // }

    return (
        // <VStack spacing={4} p={5} align="stretch">
        //     <Text fontSize="xl" fontWeight="bold">Login</Text>
        //     <form onSubmit={handleSubmit}>
        //         <FormControl isRequired>
        //             <FormLabel htmlFor="email">Email</FormLabel>
        //             <Input id="email" type="email" value={email} onChange={handleEmail} placeholder="Enter your email" />
        //         </FormControl>
        //         <FormControl isRequired mt={4}>
        //             <FormLabel htmlFor="password">Password</FormLabel>
        //             <Input id="password" type="password" value={password} onChange={handlePassword} placeholder="Enter your password" />
        //         </FormControl>
        //         {error && (
        //             <Text color="red.500">{error}</Text>
        //         )}
        //         <Button mt={4} width="full" type="submit" colorScheme="blue">Log In</Button>
        //     </form>
        //     <Box>
        //         <Text mt={2}>
        //             Didn't have an account? &nbsp;
        //             <Link color="teal.500" onClick={handleSignup}>Sign Up</Link>
        //         </Text>
        //     </Box>
        // </VStack>
        <div>
            <h1>Login</h1>
        </div>
    );
}

export default Login;
