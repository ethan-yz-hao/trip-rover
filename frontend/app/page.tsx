import PlanComponent from "@/app/components/PlanComponent";
import LoginForm from "@/app/components/LoginForm";
import Logout from "./components/Logout";
import PlanList from "./components/PlanList";
import SearchUser from "./components/SearchUser";

export default function Home() {
    return (
        <>
            <LoginForm/>
            <Logout/>
            <SearchUser/>
            {/* <PlanComponent planId={1}/> */}
            <PlanList/>
        </>
    );
}
