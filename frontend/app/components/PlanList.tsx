'use client';
import React, { useState, useEffect } from 'react';
import log from "@/app/log";
import { PlanSummary } from '@/app/model';
import Link from 'next/link';

const PlanList = () => {
  const [plans, setPlans] = useState<PlanSummary[]>([]);
  const [error, setError] = useState<string>('');

  useEffect(() => {
    const fetchPlans = async () => {
      try {
        const response = await fetch('http://localhost:8080/api/plan', {
          credentials: 'include',
        });
        
        if (!response.ok) {
          throw new Error('Please log in to view plans');
        }
        
        const data = await response.json();
        // Convert string dates to Date objects
        const plansWithDates = data.data.map((plan: PlanSummary) => ({
          ...plan,
          createTime: new Date(plan.createTime),
          updateTime: new Date(plan.updateTime),
        }));
        setPlans(plansWithDates);
      } catch (error) {
        log.error('Error fetching plans:', error);
        setError(error instanceof Error ? error.message : 'Failed to load plans');
      }
    };

    fetchPlans();
  }, []);

  if (error) {
    return <div>{error}</div>;
  }

  return (
    <div>
      <h2>Your Plans</h2>
      {plans.map(plan => (
        <div key={plan.planId} className="plan-item">
          <Link href={`/plan/${plan.planId}`}>
            <h3 className="hover:underline cursor-pointer">{plan.planName}</h3>
          </Link>
          <p>Role: {plan.role}</p>
          <p>Created: {plan.createTime.toLocaleString()}</p>
          <p>Last Modified: {plan.updateTime.toLocaleString()}</p>
        </div>
      ))}
    </div>
  );
};

export default PlanList;