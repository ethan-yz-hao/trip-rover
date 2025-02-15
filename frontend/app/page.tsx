import PlanComponent from "@/app/components/PlanComponent";
import LoginForm from "@/app/components/LoginForm";
import Logout from "./components/Logout";
import PlanList from "./components/PlanList";
import SearchUser from "./components/SearchUser";
import SearchPlan from "./components/SearchPlan";

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
