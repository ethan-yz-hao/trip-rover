"use client";
import { useState } from "react";
import PlanPlaceList from "@/components/canvas/list/PlanPlaceList";
import PlanSummary from "@/components/canvas/list/PlanSummary";
import Navbar from "@/components/navbar/Navbar";
import { useParams } from "next/navigation";

export default function PlanPage() {
    const params = useParams();
    const planId = Number(params.planId);
    const [userRole, setUserRole] = useState<"OWNER" | "EDITOR" | "VIEWER">(
        "VIEWER"
    );

    return (
        <>
            <Navbar />
            <div className="container mx-auto p-4">
                <PlanSummary planId={planId} onRoleChange={setUserRole} />
                <PlanPlaceList planId={planId} userRole={userRole} />
            </div>
        </>
    );
}
