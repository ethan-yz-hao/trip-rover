import { StoreContext } from '../context/StoreContext';
import React, { useState, useContext, useEffect } from 'react';
import { Button, Heading, Container, Box, FormLabel, FormControl, Textarea } from '@chakra-ui/react';
import { useParams } from 'react-router-dom';
import { useNavigate } from 'react-router-dom';

import { getCommentFromId } from '../context/StoreContext';

interface Comment {
    id: string | null;
    userId: string | null;
    postId: string;
    text: string;
    datetime: string;
    edit: string;
}

const CommentEdit = () => {

    const navigate = useNavigate()

    const { editComment } = useContext(StoreContext);
    const { currentUserId } = useContext(StoreContext)

    useEffect(() => {
        if (currentUserId == null) {
            navigate('/login');
            return;
        }
    }, [currentUserId])

    const { commentId } = useParams();
    const [comment, setComment] = useState<Comment | null>(null);
    useEffect(() => {
        getCommentFromId(commentId).then((data) => {
            setComment(data);

        });
    }, []);

    useEffect(() => {
        setCommentText(comment?.text);
    }, [comment]);

    const [commentText, setCommentText] = useState<string | undefined>("");


    const EditCommentSubmit = (e: React.FormEvent<HTMLDivElement>) => {
        if (currentUserId == null) {
            navigate('/login');
            return;
        }
        e.preventDefault()
        editComment(commentId, commentText);
        navigate(-1);
    }


    return <div>
        <Container height='100vh' maxW='80%' margin='auto'>
            <Heading fontSize="3xl" fontWeight={500} fontFamily="body" m="5">
                Edit Comment
            </Heading>
            <Box marginTop='2rem' flexDirection='column' alignItems='center' margin='auto' padding='1rem' boxShadow='xl' rounded='lg' paddingBlock={5} p={8} border='2px solid white'>
                <form onSubmit={(e: any) => EditCommentSubmit(e)}>
                    <FormControl id="post_content">
                        <FormLabel>Comment Content</FormLabel>
                        <Textarea value={commentText} onChange={(e) => setCommentText(e.target.value)}  />
                    </FormControl>
                    <Button mt={4} colorScheme="blue" type="submit">Edit Comment</Button>
                </form>
            </Box>
        </Container>
    </div>
}

export default CommentEdit;