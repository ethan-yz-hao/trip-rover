import PlanComponent from "@/app/components/PlanComponent";
import LoginForm from "@/app/components/LoginForm";

export default function Home() {
    return (
        <>
            <LoginForm/>
            <PlanComponent planId={1}/>
        </>
    );
}
