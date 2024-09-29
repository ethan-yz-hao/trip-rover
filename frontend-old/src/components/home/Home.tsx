import {Grid, GridItem} from "@chakra-ui/react";
import SideBar from "@/components/home/SideBar.tsx";
import Canvas from "@/components/home/Canvas.tsx";

function Home() {
    return (
        <Grid
            height="calc(100vh - 50px)"
            width="100%"
            templateAreas={{
                base: `"aside" "main"`,
                lg: `"aside main"`
            }}
            templateColumns={{
                base: '1fr',
                lg: '1fr 3fr'
            }}
            templateRows={{
                base: 'auto 1fr',
                lg: '1fr'
            }}
        >
            <GridItem area="aside">
                <SideBar/>
            </GridItem>
            <GridItem area="main">
                <Canvas/>
            </GridItem>
        </Grid>
    );
}

export default Home;