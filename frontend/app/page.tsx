import PlanList from "@/app/profile/components/PlanList";
import SearchUser from "@/components/canvas/list/SearchUser";
import SearchPlan from "@/components/canvas/list/SearchPlan";
import Navbar from "../components/navbar/Navbar";

export default function Home() {
    return (
        <>
            <Navbar />
            <SearchUser />
            <SearchPlan />
            Need a public list and map that works when not logged in and have no
            websocket connection
        </>
    );
}
