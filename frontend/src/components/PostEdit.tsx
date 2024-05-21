import { Button, Heading } from '@chakra-ui/react';
import { Input, Textarea, FormLabel, FormControl, Box, Container } from '@chakra-ui/react';
import React, { useContext, useEffect, useState } from 'react';
import { StoreContext } from '../context/StoreContext';

import { useNavigate, useParams } from 'react-router-dom';

import { getPostFromId } from '../context/StoreContext';

interface Post {
    id: string;
    userId: string | null;
    photo: string;
    desc: string;
    datetime: string;
    edit: string;
}


const PostEdit = () => {
    

    const navigate = useNavigate();
    const { postId } = useParams();
    const [postContent, setPostContent] = useState<string | undefined>("");
    const { currentUserId, editPost } = useContext(StoreContext)
    const [post, setPost] = useState<Post | null>(null);
    const [imageUpload, setImageUpload] = useState<File | null>(null);
    useEffect(() => {
        if (currentUserId == null) {
            navigate('/login');
            return;
        }
    }, [currentUserId])

    useEffect(() => {
        getPostFromId(postId).then((data) => {
            setPost(data);
        });
    }, []);

    useEffect(() => {
        setPostContent(post?.desc);
    }, [post]);

    const onPostEdit = (e: React.FormEvent<HTMLDivElement>) => {
        e.preventDefault()
        editPost(postId, postContent, imageUpload);
        navigate('/post');
    }

    return <div>
        <div style={{ height: '100%', width: '100%', display: 'flex' }}>

            <Container height='100vh' maxW='80%' margin='auto'>
                <Heading fontSize="3xl" fontWeight={500} fontFamily="body" m="5">
                    Edit Post
                </Heading>
                <Box marginTop='2rem' flexDirection='column' alignItems='center' margin='auto' padding='1rem' boxShadow='xl' rounded='lg' paddingBlock={5} p={8} border='2px solid white'>
                    <form onSubmit={(e: any) => onPostEdit(e)}>
                        <FormControl id="post_content">
                            <FormLabel>Post Content</FormLabel>
                            <Textarea value={postContent} onChange={(e) => setPostContent(e.target.value)} />
                        </FormControl>
                        <FormControl id="post_image" mt={4}>
                            <FormLabel>Change a New Image</FormLabel>
                            <Input type="file" accept=".jpg, .jpeg, .png" onChange={(e) => { setImageUpload(e.target.files == null ? null : e.target.files[0]) }} />
                        </FormControl>
                        <Button mt={4} colorScheme="blue" type="submit">Edit Post</Button>
                    </form>
                </Box>
            </Container>

        </div>
    </div>
}

export default PostEdit;