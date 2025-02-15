"use client";
import PlanComponent from "@/components/canvas/list/PlanComponent";
import PlanSummary from "@/components/canvas/list/PlanSummary";
import Navbar from "@/components/navbar/Navbar";
import { useParams } from "next/navigation";

export default function PlanPage() {
    const params = useParams();
    const planId = Number(params.planId);

    return (
        <>
            <Navbar />
            <div className="container mx-auto p-4">
                <PlanSummary planId={planId} />
                <PlanComponent planId={planId} />
            </div>
        </>
    );
}
