import SearchUser from "@/components/canvas/search/SearchUser";
import SearchPlan from "@/components/canvas/search/SearchPlan";
import Navbar from "../components/navbar/Navbar";
import CanvasContainer from "@/components/canvas/CanvasContainer";
export default function Home() {
    return (
        <>
            <Navbar />
            <SearchUser />
            <SearchPlan />
            <CanvasContainer />
        </>
    );
}
