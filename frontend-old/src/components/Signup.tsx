import React, { useState, useContext } from 'react';
import { useNavigate } from "react-router-dom";
import {
  Box,
  Button,
  FormControl,
  FormLabel,
  Input,
  Textarea,
  Image,
  VStack,
  Text,
  useToast,
  HStack
} from '@chakra-ui/react';
import FileLoader from '../utils/FileLoader'; 
import { StoreContext } from '../context/StoreContext';

interface SignupProps {}

const Signup: React.FC<SignupProps> = () => {
    const navigate = useNavigate();
    const [email, setEmail] = useState<string>('');
    const [id, setId] = useState<string>('');
    const [password, setPassword] = useState<string>('');
    const [bio, setBio] = useState<string>('');
    const [name, setName] = useState<string>('');
    const [photo, setPhoto] = useState<string>('');
    const [dragging, setDragging] = useState<boolean>(false);
    const { signup } = useContext(StoreContext);
    const toast = useToast();

    const handleSubmit = (e: React.FormEvent<HTMLDivElement>) => {
        e.preventDefault();
        if (email === '' || password === '') {
            toast({
                title: "Error",
                description: "Email and password are required.",
                status: "error",
                duration: 5000,
                isClosable: true,
            });
            return;
        }
        signup(email, password, bio, id, name, photo);
        setEmail('');
        setPassword('');
        setBio('');
        setId('');
        setName('');
        setPhoto('');
    };

    const handleLogin = () => {
        navigate('/login');
    };
    const handleFileDrop = (e: React.DragEvent<HTMLDivElement>) => {
        e.preventDefault();
        if (!e.dataTransfer.types.includes('Files')) {
            return;
        }
        if (e.dataTransfer.files.length >= 1) {
            let file = e.dataTransfer.files[0];
            if (file.size > 1000000) { // larger than 1 MB
                return;
            }
            if (file.type.match(/image.*/)) {
                let reader = new FileReader();
                reader.onloadend = (e: ProgressEvent<FileReader>) => {
                    setPhoto(e.target?.result as string);
                };
                reader.readAsDataURL(file);
            }
        }
        setDragging(false);
    };

    return (
        <VStack spacing={4} align="stretch">
            <Text fontSize="xl" fontWeight="bold">Sign Up</Text>
            <Box>
                {!photo ? (
                    <Text>Drop your photo here</Text>
                ) : (
                    <Image src={photo} alt="Profile Photo" />
                )}
                <FileLoader
                    onDragEnter={() => setDragging(true)}
                    onDragLeave={() => setDragging(false)}
                    onDrop={handleFileDrop}
                >
                    <Box p={4} border={dragging ? "2px solid blue" : "2px dashed gray"}>
                        Drop files here
                    </Box>
                </FileLoader>
            </Box>
            <form onSubmit={(e: any) => handleSubmit(e)}>
                <FormControl id="email">
                    <FormLabel>Email</FormLabel>
                    <Input type="email" value={email} onChange={(e) => setEmail(e.target.value)} />
                </FormControl>
                <FormControl id="password">
                    <FormLabel>Password</FormLabel>
                    <Input type="password" value={password} onChange={(e) => setPassword(e.target.value)} />
                </FormControl>
                <FormControl id="id">
                    <FormLabel>ID</FormLabel>
                    <Input type="text" value={id} onChange={(e) => setId(e.target.value)} />
                </FormControl>
                <FormControl id="name">
                    <FormLabel>Name</FormLabel>
                    <Input type="text" value={name} onChange={(e) => setName(e.target.value)} />
                </FormControl>
                <FormControl id="bio">
                    <FormLabel>Bio</FormLabel>
                    <Textarea value={bio} onChange={(e) => setBio(e.target.value)} />
                </FormControl>
                <Button mt={4} colorScheme="blue" type="submit">Sign Up</Button>
            </form>
            <HStack mt={4} alignContent={"center"} justifyContent={"center"}>
            <Text>Already have an account?</Text>
            <Button colorScheme="teal" onClick={handleLogin}>Log In</Button>
            </HStack>
        </VStack>
    );
}

export default Signup;
