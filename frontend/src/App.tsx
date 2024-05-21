import './App.css'
import NavBar from "@/components/NavBar.tsx";
import Home from "@/components/home/Home.tsx";
import Profile from "@/components/Profile.tsx";
import Login from "@/components/Login.tsx";
import {BrowserRouter as Router, Route, Routes} from "react-router-dom";
import Signup from './components/Signup';
import StoreContextProvider from './context/StoreContext';
import Post from "@/components/Post.tsx";
import NewPost from './components/NewPost';
import Detail from './components/Detail';
import CommentEdit from './components/CommentEdit';
import PostEdit from './components/PostEdit';

function App() {
    return (
        <Router>
            <StoreContextProvider>
                <NavBar/>
                <Routes>
                    <Route path="/profile/:userId" element={<Profile/>}/>
                    <Route path="/profile" element={<Profile/>}/>
                    <Route path="/login" element={<Login/>}/>
                    <Route path="/signup" element={<Signup/>}/>
                    <Route path="/" element={<Home/>}/>
                    <Route path="/post" element={<Post/>}/>
                    <Route path="/post/newpost" element={<NewPost/>}/>
                    <Route path={`/post/:postId`} element={<Detail />} />
                    <Route path={`/comment/:commentId`} element={<CommentEdit />} />
                    <Route path={`/postedit/:postId`} element={<PostEdit />} />
                </Routes>
            </StoreContextProvider>
        </Router>
    )
}

export default App
