import PlanComponent from "@/app/components/home/PlanComponent";
import LoginForm from "@/app/components/profile/LoginForm";
import Logout from "./components/profile/Logout";
import PlanList from "./components/home/PlanList";
import SearchUser from "./components/home/SearchUser";
import SearchPlan from "./components/home/SearchPlan";

export default function Home() {
    return (
        <>
            <LoginForm/>
            <Logout/>
            <SearchUser/>
            <SearchPlan/>
            {/* <PlanComponent planId={1}/> */}
            <PlanList/>
        </>
    );
}
