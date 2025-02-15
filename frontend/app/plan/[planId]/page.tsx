'use client';
import PlanComponent from '@/app/components/home/PlanComponent';
import PlanSummary from '@/app/components/home/PlanSummary';
import { useParams } from 'next/navigation';

export default function PlanPage() {
  const params = useParams();
  const planId = Number(params.planId);

  return (
    <div className="container mx-auto p-4">
      <PlanSummary planId={planId} />
      <PlanComponent planId={planId} />
    </div>
  );
} 