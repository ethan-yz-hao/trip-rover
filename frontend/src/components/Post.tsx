import { StoreContext } from '../context/StoreContext';
import { useContext, useEffect } from 'react';
import { Button, Heading, Container, Box } from '@chakra-ui/react';
import { Link } from 'react-router-dom';

const Post = () => {
    
    let { posts } = useContext(StoreContext);
    useEffect(() => {}, [posts])

    const groupedPosts = [];
    for (let i = 0; i < posts.length; i += 4) {
        groupedPosts.push(posts.slice(i, i + 4));
    }

    return (
        <div style={{ marginLeft: '3rem', marginRight: '3rem' }}>
            <Heading fontSize="3xl" fontWeight={500} fontFamily="body" mt="5">
                View Posts
            </Heading>
            <Button as={Link} to="/post/newpost" mt={4} colorScheme="blue" type="submit">Create New Post</Button>

            {groupedPosts.map((group, groupIndex) => (
                <div key={groupIndex} style={{ display: 'flex', marginTop: '2rem' }}>
                    {group.map((p, index) => (
                        <div key={index} style={{ flex: '0 0 calc(25% - 1rem)', margin: '0.5rem' }}>
                            <APost uid={p.userId} content={p.desc} datetime={p.datetime} photo={p.photo} index={index} postId={p.id} edit={p.edit} />
                        </div>
                    ))}
                </div>
            ))}
        </div>
    )
};


export default Post;


const APost = (props: any) => {

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

    return (
        <div>
            <div style={{ height: '100%', width: '100%', display: 'flex' }}>

                <Container maxW='100%' margin='auto'>
                    <Box bg='#fff0f3' height='380px' marginTop='2rem' flexDirection='column' textAlign='left' margin='auto' padding='1rem' rounded='lg' paddingBlock={5} p={6} border='2px solid white'>
                        <div style={{ height: '300px' }}>
                            <b>
                                {props.uid}
                            </b>
                            <p>
                                {props.edit == '' ? 'Posted at ' : 'Edited at '}{datetime}
                            </p>
                            <br />
                            {props.photo !== "" ? <p style={{ overflow: 'hidden', textOverflow: 'ellipsis', display: '-webkit-box', WebkitBoxOrient: 'vertical', WebkitLineClamp: 1 }}>
                                {props.content}
                            </p> :
                                <p style={{ overflow: 'hidden', textOverflow: 'ellipsis', display: '-webkit-box', WebkitBoxOrient: 'vertical', WebkitLineClamp: 8 }}>
                                    {props.content}
                                </p>}
                            {props.photo !== "" ? <img src={props.photo} style={{ height: '180px', width: 'auto', display: 'block', margin: '0 auto' }} /> : null}
                            <br />
                        </div>
                        <hr />

                        <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
                            <Button as={Link} to={`/post/${props.postId}`} id={`pos_${props.index}_comment_button`} textAlign='center' flex={1} boxShadow='rgba(0, 0, 0, 0.1) 0px 4px 12px' bg='#fff0f3'>
                                View
                            </Button>
                        </div>
                    </Box>
                </Container>


            </div>
        </div>
    );
};


