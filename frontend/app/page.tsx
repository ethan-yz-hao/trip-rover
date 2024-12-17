import PlanComponent from "@/app/components/PlanComponent";
import LoginForm from "@/app/components/LoginForm";
import Logout from "./components/Logout";
import PlanList from "./components/PlanList";

export default function Home() {
    return (
        <>
            <LoginForm/>
            <Logout/>
            {/* <PlanComponent planId={1}/> */}
            <PlanList/>
        </>
    );
}
