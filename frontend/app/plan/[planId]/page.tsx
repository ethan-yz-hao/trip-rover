'use client';
import PlanComponent from '@/app/components/PlanComponent';
import { useParams } from 'next/navigation';

export default function PlanPage() {
  const params = useParams();
  const planId = Number(params.planId);

  return (
    <div className="container mx-auto p-4">
      <PlanComponent planId={planId} />
    </div>
  );
} 