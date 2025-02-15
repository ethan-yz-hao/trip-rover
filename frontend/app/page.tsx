import PlanComponent from "@/components/home/PlanComponent";
import LoginForm from "@/components/profile/login/LoginForm";
import Logout from "@/components/profile/login/Logout";
import PlanList from "@/components/home/PlanList";
import SearchUser from "@/components/home/SearchUser";
import SearchPlan from "@/components/home/SearchPlan";

export default function Home() {
    return (
        <>
            <LoginForm />
            <Logout />
            <SearchUser />
            <SearchPlan />
            {/* <PlanComponent planId={1}/> */}
            <PlanList />
        </>
    );
}
