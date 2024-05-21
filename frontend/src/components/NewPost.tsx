import { Button, Heading } from '@chakra-ui/react';
import { Input, Textarea, FormLabel, FormControl, Box, Container } from '@chakra-ui/react';
import React, { useContext, useEffect, useState } from 'react';
import { StoreContext } from '../context/StoreContext';
import { useNavigate } from 'react-router-dom';

const NewPost = () => {


    const [postContent, setPostContent] = useState("");
    const [imageUpload, setImageUpload] = useState<File | null>(null);
    const { addPost, currentUserId } = useContext(StoreContext)
    const navigate = useNavigate()

    console.log(currentUserId)

    useEffect(() => {
        if (currentUserId == null) {
            navigate('/login');
            return;
        }
    }, [currentUserId])

    const newPostSubmit = (e: React.FormEvent<HTMLDivElement>) => {
        e.preventDefault()
        addPost(imageUpload, postContent);
        navigate('/post');
    }

    return (
        <div style={{height: '100%', width: '100%', display: 'flex'}}>

            <Container height='100vh' maxW='80%' margin='auto'>
                <Heading fontSize="3xl" fontWeight={500} fontFamily="body" m="5">
                    New Post
                </Heading>
                <Box marginTop='2rem' flexDirection='column' alignItems='center' margin='auto' padding='1rem' boxShadow='xl' rounded='lg' paddingBlock={5} p={8} border='2px solid white'>
                        <form onSubmit={(e: any) => newPostSubmit(e)}>
                            <FormControl id="post_content">
                                <FormLabel>Post Content</FormLabel>
                                <Textarea placeholder='Post description' value={postContent} onChange={(e) => setPostContent(e.target.value)}/>
                            </FormControl>
                            <FormControl id="post_image" mt={4} isRequired>
                                <FormLabel>Upload Image</FormLabel>
                                <Input type="file" accept=".jpg, .jpeg, .png" onChange={(e) => {setImageUpload(e.target.files == null ? null : e.target.files[0])}} />
                            </FormControl>
                            <Button mt={4} colorScheme="blue" type="submit">Create New Post</Button>
                        </form>
                </Box>
            </Container>

        </div>
    )
};

export default NewPost;