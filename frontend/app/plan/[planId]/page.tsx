"use client";
import Navbar from "@/components/navbar/Navbar";
import { useParams } from "next/navigation";
import CanvasContainer from "@/components/canvas/CanvasContainer";

export default function PlanPage() {
    const params = useParams();
    const planId = Number(params.planId);

    return (
        <>
            <Navbar />
            <CanvasContainer planId={planId} />
        </>
    );
}
