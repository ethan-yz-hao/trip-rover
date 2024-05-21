import { StoreContext } from '../context/StoreContext';
import React, { useState, useContext, useEffect } from 'react';
import { Button, Container, Box, FormControl, Textarea } from '@chakra-ui/react';
import { Link, useParams } from 'react-router-dom';
import { useNavigate } from 'react-router-dom';

import { getLikesForPost } from '../context/StoreContext';
// import { getCommentsForPost } from '../context/StoreContext';
import { getPostFromId } from '../context/StoreContext';
// import uniqueId from '../utils/uniqueId';

interface Post {
    id: string;
    userId: string | null;
    photo: string;
    desc: string;
    datetime: string;
    edit: string;
}

const Detail = () => {
    const { postId } = useParams();
    const [post, setPost] = useState<Post | null>(null);

    useEffect(() => {
        getPostFromId(postId).then((data) => {
            setPost(data);
        });
    }, [postId]);

    if (!post) {
        return <div></div>;
    }

    return (
        <div>
            <div style={{ display: 'flex', flexDirection: 'row', marginLeft: '5rem', marginRight: '5rem' }}>
                <div style={{ flex: 1, borderRight: "1px dotted grey", display: 'flex', justifyContent: 'center', alignItems: 'center' }}>
                    <div style={{ minWidth: '80%', minHeight: '80%' }}>
                        <APost uid={post.userId} content={post.desc} datetime={post.datetime} photo={post.photo} index={0} postId={post.id} userId={post.userId} edit={post.edit} />
                    </div>
                </div>
                <div style={{ flex: 1, display: 'flex', justifyContent: 'center', alignItems: 'center' }}>
                    <div style={{ minWidth: '100%', minHeight: '80%' }}>
                        <Comment postId={postId} />
                    </div>
                </div>
            </div>
        </div>
    );
};

export default Detail;


interface Like {
    userId: string | null;
    postId: string;
    datetime: string;
}

interface Comment {
    id: string | null;
    userId: string | null;
    postId: string;
    text: string;
    datetime: string;
    edit: string;
}

const APost = (props: any) => {
    const navigate = useNavigate()
    let { addLike, removeLike, deletePost } = useContext(StoreContext);
    const { currentUserId } = useContext(StoreContext)
    const [likesForPost, setLikesForPost] = useState<Like[]>([]);

    useEffect(() => {
        getLikesForPost(props.postId).then(data => {
            setLikesForPost(data)
        });
    }, [])


    const onPostDelete = (e: React.FormEvent<HTMLDivElement>, postId: string) => {
        e.preventDefault()
        if (currentUserId == null) {
            navigate('/login');
            return;
        }
        deletePost(postId);
        navigate('/post');
    }

    const likeExists = (postId: string) => {
        let likeExist = false
        for (let i = 0; i < likesForPost.length; i++) {
            const like = likesForPost[i];
            if ((like.postId == postId) && (like.userId == currentUserId)) {
                likeExist = true
            }
        }
        return likeExist
    }

    const likeToggle = (postId: string) => {
        
        if (currentUserId == null) {
            navigate('/login');
            return;
        }
        const likeExist = likeExists(postId);

        const elem = document.getElementById(`post_${postId}_like`)
        if (elem && !likeExist) {
            const newLike: Like = {
                userId: currentUserId,
                postId,
                datetime: new Date().toISOString(),
              };
            setLikesForPost(likesForPost.concat(newLike));
            // elem.innerHTML = `&hearts; ${likesForPost.length}`;
            elem.style.color = 'red';
            addLike(postId);
        }
        if (elem && likeExist) {
            setLikesForPost(
                likesForPost.filter(
                  (alike) => !(alike.userId === currentUserId && alike.postId === postId)
                )
            );
            removeLike(postId)
            // elem.innerHTML = `&#9825; ${likesForPost.length}`;
            elem.style.color = 'black';
        }
    }
    let dateobj = new Date(props.datetime)
    let date = dateobj.toLocaleDateString('en-US', {
        month: '2-digit',
        day: '2-digit',
        year: 'numeric'
    });
    let time = dateobj.toLocaleTimeString('en-US', {
        hour12: false,
        hour: '2-digit',
        minute: '2-digit'
    })
    const postDatetime = `${date} ${time}`

    if (props.edit != '') {
        dateobj = new Date(props.edit)
    }
    date = dateobj.toLocaleDateString('en-US', {
        month: '2-digit',
        day: '2-digit',
        year: 'numeric'
    });
    time = dateobj.toLocaleTimeString('en-US', {
        hour12: false,
        hour: '2-digit',
        minute: '2-digit'
    })
    const editDatetime = `${date} ${time}`

    let initialHeart = 1;
    let initialColour = 'black';
    if (!likeExists(props.postId)) {
        initialHeart = 1;
        initialColour = 'black';
    }
    else {
        initialHeart = 2;
        initialColour = 'red';
    }


    return (
        <div>
            <div style={{ height: '100%', width: '100%', display: 'flex' }}>

                <Container maxW='100%' margin='auto'>
                    <Box marginTop='2rem' flexDirection='column' textAlign='left' margin='auto' padding='1rem' paddingBlock={5} p={6}>
                        {/* <div style={{ height: '300px' }}> */}
                        <b>{props.uid}</b>
                        <p>Posted at {postDatetime}</p>
                        
                        <p>{props.edit == '' ? '' : `Edited at ${editDatetime}`}</p>
                        <br />
                        <p> {props.content} </p>
                        <br />
                        {props.photo !== "" ? <img src={props.photo} style={{ maxHeight: '350px', width: 'auto', display: 'block', margin: '0 auto' }} /> : null}
                        <br />
                        {/* </div> */}
                        <hr />

                        <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
                            <Button onClick={() => likeToggle(props.postId)} id={`post_${props.postId}_like`} textAlign='center' flex={1} boxShadow='rgba(0, 0, 0, 0.1) 0px 4px 12px'>
                                <div style={{ color: initialColour }} id={`post_${props.postId}_heart`}>
                                    {initialHeart == 1 ? <div>&#9825; {likesForPost.length}</div> : <div>&hearts; {likesForPost.length}</div>}
                                </div>
                            </Button>
                        </div>
                        <br />
                        {/* <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}> */}
                        {currentUserId == props.userId ?
                            <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
                                <Button as={Link} to={`/postedit/${props.postId}`} textAlign='center' flex={1} boxShadow='rgba(0, 0, 0, 0.1) 0px 4px 12px' marginRight='1rem'>
                                    Edit
                                </Button>

                                <Button onClick={(e: any) => onPostDelete(e, props.postId)} textAlign='center' flex={1} boxShadow='rgba(0, 0, 0, 0.1) 0px 4px 12px' marginLeft='1rem'>
                                    Delete
                                </Button>
                            </div> :
                            <div></div>
                        }
                        {/* </div> */}
                    </Box>
                </Container>


            </div>

        </div>
    );
};

const Comment = (props: any) => {

    const navigate = useNavigate()
    let { comments, addComment } = useContext(StoreContext);
    const { currentUserId } = useContext(StoreContext)
    const [commentContent, setCommentContent] = useState("");
    const [commentsForPost, setCommentsForPost] = useState<(Comment | undefined)[]>([]);
    let commentChanged = 0;

    const filterCommentsForPosts = (comments: (Comment | undefined)[]) => {

        const filtered = comments.filter((comment) => comment?.postId == props.postId)
        console.log(filtered)
        setCommentsForPost(filtered)
        console.log(commentsForPost)
    }

    useEffect(() => {
        filterCommentsForPosts(comments as (Comment | undefined)[])
        console.log(commentsForPost)
    }, [comments])

    const newCommentSubmit = (e: React.FormEvent<HTMLDivElement>, postId: string) => {
        e.preventDefault()
        if (currentUserId == null) {
            navigate('/login');
            return;
        }
        console.log(postId);
        console.log(commentContent);
        addComment(postId, commentContent);
        setCommentContent("");
        commentChanged++;
    }

    return <div>
        <div id={`post_${props.postId}_add_comment_area`} style={{ backgroundColor: 'white', paddingTop: '4rem', paddingLeft: '2rem' }}>
            <form onSubmit={(e: any) => newCommentSubmit(e, props.postId)}>
                <FormControl>
                    {/* <FormLabel>Add Comment</FormLabel> */}
                    <Textarea placeholder='Add comment' value={commentContent} onChange={(e) => setCommentContent(e.target.value)} isRequired/>
                </FormControl>
                <Button mt={4} colorScheme="blue" boxShadow='rgba(0, 0, 0, 0.1) 0px 4px 12px' type="submit">Comment</Button>
            </form>
        </div>

        <Container maxW='100%' margin='auto'>
            <Box marginTop='2rem' flexDirection='column' textAlign='left' margin='auto' rounded='lg' paddingTop='2rem' paddingLeft='2rem'>
                <div style={{ textAlign: 'center', fontSize: '1.2rem' }}> Comments ({commentsForPost.length}) </div>
                {commentsForPost.length == 0 ?
                    <div style={{ textAlign: 'center' }}>
                        <hr />
                        <br />
                        No Comments Yet
                    </div>
                    : commentsForPost.map((c: Comment | undefined, index: number) => {
                        return <div key={index}>
                            <AComment uid={c?.userId} datetime={c?.datetime} content={c?.text} postId={c?.postId} commentId={c?.id} edit={c?.edit} commentsForPost={commentsForPost} setCommentsForPost={setCommentsForPost} />
                        </div>
                    })}
            </Box>
        </Container>
    </div>
}
const AComment = (props: any) => {

    const { currentUserId, deleteComment } = useContext(StoreContext)

    const navigate = useNavigate()
    const onCommentDelete = (e: React.FormEvent<HTMLDivElement>, commentId: string) => {
        e.preventDefault()
        if (currentUserId == null) {
            navigate('/login');
            return;
        }
        deleteComment(commentId);
    }


    let dateobj = new Date(props.datetime)
    if (props.edit != '') {
        dateobj = new Date(props.edit)
    }
    const date = dateobj.toLocaleDateString('en-US', {
        month: '2-digit',
        day: '2-digit',
        year: 'numeric'
    });
    const time = dateobj.toLocaleTimeString('en-US', {
        hour12: false,
        hour: '2-digit',
        minute: '2-digit'
    })
    const datetime = `${date} ${time}`

    return <div style={{ marginBottom: '1.3rem' }}>
        <hr />
        <div style={{ marginTop: '5px', marginBottom: '1rem' }}>
            <b>{props.uid}</b> &nbsp; &nbsp; {props.edit == '' ? 'Commented at ' : 'Edited at '} {datetime}
            <br />
            <p>
                {props.content}
            </p>
        </div>

        {props.uid == currentUserId ?
            <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
                <Button as={Link} to={`/comment/${props.commentId}`} height='1.2rem' textAlign='center' flex={1} boxShadow='rgba(0, 0, 0, 0.1) 0px 4px 12px' marginRight='1rem'>
                    Edit
                </Button>

                <Button onClick={(e: any) => onCommentDelete(e, props.commentId)} height='1.2rem' flex={1} boxShadow='rgba(0, 0, 0, 0.1) 0px 4px 12px' marginLeft='1rem'>
                    Delete
                </Button>
            </div> :
            <div></div>
        }


    </div>

}