import PlanComponent from "@/app/components/PlanComponent";
import LoginForm from "@/app/components/LoginForm";
import Logout from "./components/Logout";

export default function Home() {
    return (
        <>
            <LoginForm/>
            <Logout/>
            <PlanComponent planId={1}/>
        </>
    );
}
