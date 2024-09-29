import {Button, Stack} from "@chakra-ui/react";
import {Link} from "react-router-dom";


function NavBar() {
    return (
        <nav>
            <Stack backgroundColor='#CFDBE6' spacing={8} direction='row' alignContent={"center"} justifyContent={"center"}
                   height="50px"
            >
                <Button colorScheme='teal' size='md'>
                    <Link to="/">
                        Home
                    </Link>
                </Button>
                <Button colorScheme='teal' size='md'>
                    <Link to="/post">
                        Post
                    </Link>
                </Button>
                <Button colorScheme='teal' size='md'>
                <Link to="/profile">
                        Profile
                    </Link>
                </Button>
            </Stack>
        </nav>
    );
}

export default NavBar;