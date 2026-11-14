"use client";

import React, { useEffect, useState } from "react";
import { useParams, useRouter } from "next/navigation";
import Link from "next/link";
import {
  ArrowLeft,
  Briefcase,
  Calendar,
  Clock,
  ExternalLink,
  FileText,
  Video,
  Sparkles,
  History,
  Trash2,
  CheckCircle2,
  Plus,
  Send,
  HelpCircle,
  Copy,
  Check,
  Edit3
} from "lucide-react";
import { api } from "@/lib/api";
import {
  Application,
  ApplicationActivity,
  ApplicationDocument,
  ApplicationInterview,
  ApplicationQuestionResponse,
  ApplicationStatus,
  ApplicationPriority,
  DocumentType,
  InterviewStageType,
  InterviewStatus
} from "@/lib/types";
import { Button } from "@/components/ui/button";
import { Badge } from "@/components/ui/badge";
import { Input } from "@/components/ui/input";

export default function ApplicationDetailPage() {
  const params = useParams();
  const router = useRouter();
  const applicationId = params.id as string;

  const [application, setApplication] = useState<Application | null>(null);
  const [activities, setActivities] = useState<ApplicationActivity[]>([]);
  const [documents, setDocuments] = useState<ApplicationDocument[]>([]);
  const [interviews, setInterviews] = useState<ApplicationInterview[]>([]);
  const [loading, setLoading] = useState(true);

  // Tabs
  const [activeTab, setActiveTab] = useState<"overview" | "documents" | "interviews" | "qa" | "activities">("overview");

  // Document generation modal
  const [showDocModal, setShowDocModal] = useState(false);
  const [docType, setDocType] = useState<DocumentType>("COVER_LETTER");
  const [docTitle, setDocTitle] = useState("");
  const [docInstructions, setDocInstructions] = useState("");
  const [generatingDoc, setGeneratingDoc] = useState(false);

  // Selected document preview
  const [selectedDoc, setSelectedDoc] = useState<ApplicationDocument | null>(null);
  const [copiedDoc, setCopiedDoc] = useState(false);

  // Interview scheduling modal
  const [showInterviewModal, setShowInterviewModal] = useState(false);
  const [stageType, setStageType] = useState<InterviewStageType>("TECHNICAL_INTERVIEW");
  const [interviewDate, setInterviewDate] = useState("");
  const [duration, setDuration] = useState(60);
  const [interviewer, setInterviewer] = useState("");
  const [meetingLink, setMeetingLink] = useState("");
  const [prepNotes, setPrepNotes] = useState("");
  const [schedulingInterview, setSchedulingInterview] = useState(false);

  // Interview Q&A Assistant state
  const [qaInput, setQaInput] = useState("");
  const [qaLoading, setQaLoading] = useState(false);
  const [qaResponse, setQaResponse] = useState<ApplicationQuestionResponse | null>(null);

  // Custom activity note
  const [customNote, setCustomNote] = useState("");
  const [addingNote, setAddingNote] = useState(false);

  const loadData = async () => {
    try {
      setLoading(true);
      const [app, acts, docs, ints] = await Promise.all([
        api.applications.get(applicationId),
        api.applications.getActivities(applicationId),
        api.applications.getDocuments(applicationId),
        api.applications.getInterviews(applicationId),
      ]);
      setApplication(app);
      setActivities(acts);
      setDocuments(docs);
      setInterviews(ints);
      if (docs.length > 0 && !selectedDoc) {
        setSelectedDoc(docs[0]);
      }
    } catch (err) {
      console.error("Failed to load application detail", err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    if (applicationId) loadData();
  }, [applicationId]);

  const handleStatusChange = async (nextStatus: ApplicationStatus) => {
    if (!application) return;
    try {
      const updated = await api.applications.update(application.id, { status: nextStatus });
      setApplication(updated);
      const acts = await api.applications.getActivities(application.id);
      setActivities(acts);
    } catch (err) {
      console.error(err);
    }
  };

  const handleGenerateDocument = async (e: React.FormEvent) => {
    e.preventDefault();
    setGeneratingDoc(true);
    try {
      const doc = await api.applications.generateDocument(applicationId, {
        documentType: docType,
        title: docTitle.trim() || undefined,
        userInstructions: docInstructions.trim() || undefined,
      });
      setDocuments((prev) => [doc, ...prev]);
      setSelectedDoc(doc);
      setShowDocModal(false);
      setDocInstructions("");
      // Refresh activities
      const acts = await api.applications.getActivities(applicationId);
      setActivities(acts);
    } catch (err) {
      console.error(err);
    } finally {
      setGeneratingDoc(false);
    }
  };

  const handleScheduleInterview = async (e: React.FormEvent) => {
    e.preventDefault();
    setSchedulingInterview(true);
    try {
      const created = await api.applications.scheduleInterview(applicationId, {
        stageType,
        scheduledAt: interviewDate ? new Date(interviewDate).toISOString() : undefined,
        durationMinutes: duration,
        interviewerInfo: interviewer.trim() || undefined,
        meetingLink: meetingLink.trim() || undefined,
        preparationNotes: prepNotes.trim() || undefined,
      });
      setInterviews((prev) => [...prev, created]);
      setShowInterviewModal(false);
      // Refresh activities & application
      const [app, acts] = await Promise.all([
        api.applications.get(applicationId),
        api.applications.getActivities(applicationId),
      ]);
      setApplication(app);
      setActivities(acts);
    } catch (err) {
      console.error(err);
    } finally {
      setSchedulingInterview(false);
    }
  };

  const handleAskQuestion = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!qaInput.trim()) return;
    setQaLoading(true);
    try {
      const res = await api.applications.answerQuestion({
        question: qaInput.trim(),
        context: `${application?.title} at ${application?.organization}`,
      });
      setQaResponse(res);
    } catch (err) {
      console.error(err);
    } finally {
      setQaLoading(false);
    }
  };

  const handleAddCustomNote = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!customNote.trim()) return;
    setAddingNote(true);
    try {
      const act = await api.applications.createActivity(applicationId, {
        activityType: "NOTE_ADDED",
        description: customNote.trim(),
      });
      setActivities((prev) => [act, ...prev]);
      setCustomNote("");
    } catch (err) {
      console.error(err);
    } finally {
      setAddingNote(false);
    }
  };

  const handleDelete = async () => {
    if (!confirm("Are you sure you want to delete this application and its history?")) return;
    try {
      await api.applications.delete(applicationId);
      router.push("/applications");
    } catch (err) {
      console.error(err);
    }
  };

  const copyDocToClipboard = () => {
    if (!selectedDoc) return;
    navigator.clipboard.writeText(selectedDoc.content);
    setCopiedDoc(true);
    setTimeout(() => setCopiedDoc(false), 2000);
  };

  if (loading || !application) {
    return (
      <div className="p-12 text-center text-xs font-mono text-text-muted">
        Loading application workspace...
      </div>
    );
  }

  return (
    <div className="space-y-6">
      {/* Top breadcrumb & actions */}
      <div className="flex items-center justify-between">
        <Link
          href="/applications"
          className="inline-flex items-center gap-1.5 text-xs font-mono text-text-muted hover:text-text-primary transition-colors"
        >
          <ArrowLeft className="h-3.5 w-3.5" /> Back to Applications Pipeline
        </Link>

        <button
          onClick={handleDelete}
          className="text-xs font-mono text-danger hover:underline flex items-center gap-1"
        >
          <Trash2 className="h-3.5 w-3.5" /> Delete Application
        </button>
      </div>

      {/* Hero Header */}
      <div className="p-5 rounded-lg border border-surface-border bg-surface-card space-y-4">
        <div className="flex flex-col md:flex-row md:items-center justify-between gap-4">
          <div>
            <div className="flex items-center gap-2 flex-wrap">
              <span className="font-mono text-xs uppercase tracking-widest text-brand font-bold">
                {application.organization}
              </span>
              <Badge variant="outline" className="font-mono text-[10px]">
                {application.applicationType}
              </Badge>
              <Badge
                variant={
                  application.priority === "HIGH"
                    ? "danger"
                    : application.priority === "MEDIUM"
                    ? "warning"
                    : "outline"
                }
                className="font-mono text-[10px]"
              >
                {application.priority} Priority
              </Badge>
            </div>
            <h1 className="text-2xl font-mono font-bold text-text-primary mt-1">
              {application.title}
            </h1>
          </div>

          {/* Status selector */}
          <div className="flex items-center gap-2">
            <span className="text-xs font-mono text-text-muted">Status:</span>
            <select
              value={application.status}
              onChange={(e) => handleStatusChange(e.target.value as ApplicationStatus)}
              className="px-3 py-1.5 bg-bg-main border border-surface-border rounded font-mono text-xs text-text-primary font-bold focus:outline-none focus:border-brand"
            >
              <option value="INTERESTED">Interested</option>
              <option value="PREPARING">Preparing</option>
              <option value="READY_TO_APPLY">Ready to Apply</option>
              <option value="APPLIED">Applied</option>
              <option value="ASSESSMENT">Assessment</option>
              <option value="INTERVIEW">Interview</option>
              <option value="OFFER_RECEIVED">Offer Received</option>
              <option value="REJECTED">Rejected</option>
              <option value="WITHDRAWN">Withdrawn</option>
              <option value="ACCEPTED">Accepted</option>
            </select>
          </div>
        </div>

        {/* Telemetry row */}
        <div className="flex flex-wrap items-center gap-6 text-xs font-mono text-text-muted pt-3 border-t border-surface-border/60">
          {application.deadline && (
            <span className="flex items-center gap-1.5 text-text-secondary">
              <Calendar className="h-3.5 w-3.5 text-warning" />
              Deadline: {new Date(application.deadline).toLocaleDateString()}
            </span>
          )}
          {application.appliedAt && (
            <span className="flex items-center gap-1.5">
              <Clock className="h-3.5 w-3.5 text-brand" />
              Applied: {new Date(application.appliedAt).toLocaleDateString()}
            </span>
          )}
          {application.applicationUrl && (
            <a
              href={application.applicationUrl}
              target="_blank"
              rel="noreferrer"
              className="flex items-center gap-1 text-brand hover:underline"
            >
              Job Posting Portal <ExternalLink className="h-3 w-3" />
            </a>
          )}
          {application.roadmapId && (
            <Link
              href={`/roadmaps/${application.roadmapId}`}
              className="flex items-center gap-1 text-brand hover:underline"
            >
              Linked Preparation Roadmap <Sparkles className="h-3 w-3" />
            </Link>
          )}
        </div>
      </div>

      {/* Tabs */}
      <div className="flex items-center gap-2 border-b border-surface-border">
        <button
          onClick={() => setActiveTab("overview")}
          className={`px-4 py-2 font-mono text-xs border-b-2 transition-colors ${
            activeTab === "overview"
              ? "border-brand text-brand font-semibold"
              : "border-transparent text-text-secondary hover:text-text-primary"
          }`}
        >
          Overview & Strategy
        </button>
        <button
          onClick={() => setActiveTab("documents")}
          className={`px-4 py-2 font-mono text-xs border-b-2 transition-colors ${
            activeTab === "documents"
              ? "border-brand text-brand font-semibold"
              : "border-transparent text-text-secondary hover:text-text-primary"
          }`}
        >
          Documents & Proposals ({documents.length})
        </button>
        <button
          onClick={() => setActiveTab("interviews")}
          className={`px-4 py-2 font-mono text-xs border-b-2 transition-colors ${
            activeTab === "interviews"
              ? "border-brand text-brand font-semibold"
              : "border-transparent text-text-secondary hover:text-text-primary"
          }`}
        >
          Interviews & Rounds ({interviews.length})
        </button>
        <button
          onClick={() => setActiveTab("qa")}
          className={`px-4 py-2 font-mono text-xs border-b-2 transition-colors ${
            activeTab === "qa"
              ? "border-brand text-brand font-semibold"
              : "border-transparent text-text-secondary hover:text-text-primary"
          }`}
        >
          Interview Q&A Advisor
        </button>
        <button
          onClick={() => setActiveTab("activities")}
          className={`px-4 py-2 font-mono text-xs border-b-2 transition-colors ${
            activeTab === "activities"
              ? "border-brand text-brand font-semibold"
              : "border-transparent text-text-secondary hover:text-text-primary"
          }`}
        >
          Timeline & Audit Log ({activities.length})
        </button>
      </div>

      {/* Tab 1: OVERVIEW */}
      {activeTab === "overview" && (
        <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
          <div className="md:col-span-2 space-y-4">
            <div className="p-4 rounded-lg border border-surface-border bg-surface-card space-y-2">
              <h3 className="font-mono text-xs uppercase tracking-wider text-text-muted font-semibold">
                Application Notes & Preparation Strategy
              </h3>
              <p className="font-mono text-xs text-text-primary whitespace-pre-wrap leading-relaxed">
                {application.userNotes || "No notes recorded yet. Add your preparation targets or interview prep plan below."}
              </p>
            </div>

            {/* Quick add custom note */}
            <div className="p-4 rounded-lg border border-surface-border bg-surface-card space-y-3">
              <h3 className="font-mono text-xs uppercase tracking-wider text-text-muted font-semibold">
                Add Timeline Note / Update
              </h3>
              <form onSubmit={handleAddCustomNote} className="space-y-2">
                <textarea
                  rows={2}
                  value={customNote}
                  onChange={(e) => setCustomNote(e.target.value)}
                  placeholder="Record an update: recruiter outreach, referral contact, assessment link..."
                  className="w-full p-2.5 bg-bg-main border border-surface-border rounded text-xs font-mono text-text-primary focus:outline-none focus:border-brand"
                />
                <div className="flex justify-end">
                  <Button type="submit" size="sm" disabled={addingNote || !customNote.trim()} className="font-mono text-xs">
                    {addingNote ? "Adding..." : "Log Note"}
                  </Button>
                </div>
              </form>
            </div>
          </div>

          <div className="space-y-4">
            <div className="p-4 rounded-lg border border-surface-border bg-surface-card space-y-3 font-mono text-xs">
              <h3 className="uppercase tracking-wider text-text-muted font-semibold text-[11px]">
                Application Metadata
              </h3>
              <div className="space-y-2 text-text-secondary">
                <div className="flex justify-between">
                  <span className="text-text-muted">Target Company:</span>
                  <span className="text-text-primary font-semibold">{application.organization}</span>
                </div>
                <div className="flex justify-between">
                  <span className="text-text-muted">Priority:</span>
                  <span className="text-text-primary">{application.priority}</span>
                </div>
                <div className="flex justify-between">
                  <span className="text-text-muted">Created:</span>
                  <span>{new Date(application.createdAt).toLocaleDateString()}</span>
                </div>
                <div className="flex justify-between">
                  <span className="text-text-muted">Documents:</span>
                  <span>{documents.length} tailored files</span>
                </div>
                <div className="flex justify-between">
                  <span className="text-text-muted">Interviews:</span>
                  <span>{interviews.length} stages</span>
                </div>
              </div>
            </div>
          </div>
        </div>
      )}

      {/* Tab 2: DOCUMENTS */}
      {activeTab === "documents" && (
        <div className="space-y-4">
          <div className="flex items-center justify-between">
            <p className="font-mono text-xs text-text-muted">
              AI-generated, tailored application documents strictly grounded in your verified developer profile.
            </p>
            <Button size="sm" onClick={() => setShowDocModal(true)} className="font-mono text-xs">
              <Plus className="h-3.5 w-3.5 mr-1" />
              Generate Document
            </Button>
          </div>

          {documents.length === 0 ? (
            <div className="p-12 rounded-lg border border-surface-border bg-surface-card text-center space-y-3">
              <FileText className="h-8 w-8 text-text-muted mx-auto" />
              <p className="font-mono text-xs text-text-muted">
                No documents generated yet. Click &quot;Generate Document&quot; to draft a tailored cover letter or GSoC proposal.
              </p>
              <Button size="sm" onClick={() => setShowDocModal(true)} className="font-mono text-xs">
                Draft Cover Letter / Proposal
              </Button>
            </div>
          ) : (
            <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
              {/* Document list */}
              <div className="space-y-2">
                {documents.map((doc) => (
                  <div
                    key={doc.id}
                    onClick={() => setSelectedDoc(doc)}
                    className={`p-3 rounded border font-mono text-xs cursor-pointer transition-colors ${
                      selectedDoc?.id === doc.id
                        ? "bg-brand/10 border-brand text-brand"
                        : "bg-surface-card border-surface-border text-text-secondary hover:border-surface-hover"
                    }`}
                  >
                    <div className="flex items-center justify-between">
                      <span className="font-bold truncate">{doc.title}</span>
                      <span className="text-[10px] text-text-muted">{doc.version}</span>
                    </div>
                    <div className="text-[10px] text-text-muted mt-1">
                      {new Date(doc.createdAt).toLocaleDateString()} • {doc.documentType}
                    </div>
                  </div>
                ))}
              </div>

              {/* Document preview pane */}
              <div className="md:col-span-2 p-4 rounded-lg border border-surface-border bg-surface-card space-y-4">
                {selectedDoc ? (
                  <>
                    <div className="flex items-center justify-between border-b border-surface-border pb-3">
                      <div>
                        <h3 className="font-mono text-sm font-bold text-text-primary">
                          {selectedDoc.title}
                        </h3>
                        <span className="font-mono text-[10px] text-text-muted">
                          Type: {selectedDoc.documentType} • Version: {selectedDoc.version}
                        </span>
                      </div>

                      <Button
                        variant="outline"
                        size="sm"
                        onClick={copyDocToClipboard}
                        className="font-mono text-xs"
                      >
                        {copiedDoc ? (
                          <>
                            <Check className="h-3.5 w-3.5 mr-1 text-brand" /> Copied!
                          </>
                        ) : (
                          <>
                            <Copy className="h-3.5 w-3.5 mr-1" /> Copy Markdown
                          </>
                        )}
                      </Button>
                    </div>

                    <div className="p-4 rounded bg-bg-main border border-surface-border font-mono text-xs text-text-primary whitespace-pre-wrap leading-relaxed max-h-[500px] overflow-y-auto">
                      {selectedDoc.content}
                    </div>
                  </>
                ) : (
                  <div className="p-8 text-center font-mono text-xs text-text-muted">
                    Select a document to preview
                  </div>
                )}
              </div>
            </div>
          )}
        </div>
      )}

      {/* Tab 3: INTERVIEWS */}
      {activeTab === "interviews" && (
        <div className="space-y-4">
          <div className="flex items-center justify-between">
            <p className="font-mono text-xs text-text-muted">
              Schedule, track, and record outcomes for online assessments, technical coding rounds, and behavioral interviews.
            </p>
            <Button size="sm" onClick={() => setShowInterviewModal(true)} className="font-mono text-xs">
              <Plus className="h-3.5 w-3.5 mr-1" />
              Schedule Round
            </Button>
          </div>

          {interviews.length === 0 ? (
            <div className="p-12 rounded-lg border border-surface-border bg-surface-card text-center space-y-3">
              <Video className="h-8 w-8 text-text-muted mx-auto" />
              <p className="font-mono text-xs text-text-muted">
                No interview rounds scheduled yet. Click &quot;Schedule Round&quot; when invited to an OA or technical round.
              </p>
              <Button size="sm" onClick={() => setShowInterviewModal(true)} className="font-mono text-xs">
                Schedule Interview / OA
              </Button>
            </div>
          ) : (
            <div className="space-y-3">
              {interviews.map((int) => (
                <div
                  key={int.id}
                  className="p-4 rounded-lg border border-surface-border bg-surface-card space-y-3 font-mono text-xs"
                >
                  <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-2">
                    <div className="space-y-1">
                      <div className="flex items-center gap-2">
                        <Badge variant="secondary" className="font-mono text-[10px]">
                          {int.stageType}
                        </Badge>
                        <Badge
                          variant={
                            int.status === "PASSED"
                              ? "success"
                              : int.status === "FAILED"
                              ? "danger"
                              : "outline"
                          }
                          className="font-mono text-[10px]"
                        >
                          {int.status}
                        </Badge>
                      </div>
                      <div className="text-text-muted text-[11px]">
                        {int.scheduledAt ? new Date(int.scheduledAt).toLocaleString() : "Date TBD"} • {int.durationMinutes} mins
                      </div>
                    </div>

                    {int.meetingLink && (
                      <a
                        href={int.meetingLink}
                        target="_blank"
                        rel="noreferrer"
                        className="text-brand hover:underline flex items-center gap-1 text-xs"
                      >
                        Join Call <ExternalLink className="h-3 w-3" />
                      </a>
                    )}
                  </div>

                  {int.preparationNotes && (
                    <div className="p-2.5 rounded bg-bg-main border border-surface-border text-text-secondary">
                      <span className="text-text-muted font-bold block mb-1">Preparation Target:</span>
                      {int.preparationNotes}
                    </div>
                  )}

                  {int.interviewerInfo && (
                    <div className="text-text-muted text-[11px]">
                      Interviewer: <span className="text-text-secondary">{int.interviewerInfo}</span>
                    </div>
                  )}
                </div>
              ))}
            </div>
          )}
        </div>
      )}

      {/* Tab 4: INTERVIEW Q&A ASSISTANT */}
      {activeTab === "qa" && (
        <div className="space-y-4 max-w-3xl">
          <div className="p-4 rounded-lg border border-surface-border bg-surface-card space-y-3">
            <div>
              <h3 className="font-mono text-sm font-bold text-text-primary flex items-center gap-2">
                <HelpCircle className="h-4 w-4 text-brand" /> Interview Question Answering Advisor
              </h3>
              <p className="font-mono text-xs text-text-muted mt-1">
                Enter an application form essay question, behavioral interview prompt, or technical inquiry. The advisor synthesizes structured talking points grounded strictly in developer best practices.
              </p>
            </div>

            <form onSubmit={handleAskQuestion} className="space-y-3">
              <textarea
                rows={3}
                value={qaInput}
                onChange={(e) => setQaInput(e.target.value)}
                placeholder="e.g. 'Why are you interested in this engineering role?' or 'Describe a time you solved a complex concurrency defect.'"
                className="w-full p-2.5 bg-bg-main border border-surface-border rounded text-xs font-mono text-text-primary focus:outline-none focus:border-brand"
                required
              />
              <div className="flex justify-end">
                <Button type="submit" disabled={qaLoading || !qaInput.trim()} className="font-mono text-xs">
                  <Send className="h-3.5 w-3.5 mr-1" />
                  {qaLoading ? "Generating..." : "Get Advisory Answer"}
                </Button>
              </div>
            </form>
          </div>

          {qaResponse && (
            <div className="p-4 rounded-lg border border-surface-border bg-surface-card space-y-4 font-mono text-xs">
              <div className="border-b border-surface-border pb-2">
                <span className="text-[10px] uppercase text-brand font-semibold">Question:</span>
                <p className="text-text-primary font-bold mt-0.5">{qaResponse.question}</p>
              </div>

              <div>
                <span className="text-[10px] uppercase text-text-muted font-semibold block mb-1">
                  Suggested Approach:
                </span>
                <p className="text-text-secondary leading-relaxed bg-bg-main p-3 rounded border border-surface-border">
                  {qaResponse.suggestedAnswer}
                </p>
              </div>

              <div>
                <span className="text-[10px] uppercase text-text-muted font-semibold block mb-1">
                  Key Talking Points:
                </span>
                <ul className="list-disc list-inside space-y-1 text-text-secondary">
                  {qaResponse.keyTalkingPoints.map((pt, i) => (
                    <li key={i}>{pt}</li>
                  ))}
                </ul>
              </div>

              <div className="p-2.5 rounded bg-warning/10 border border-warning/30 text-warning text-[11px]">
                {qaResponse.advisoryNotice}
              </div>
            </div>
          )}
        </div>
      )}

      {/* Tab 5: ACTIVITIES AUDIT LOG */}
      {activeTab === "activities" && (
        <div className="space-y-3">
          {activities.length === 0 ? (
            <div className="p-8 text-center text-xs font-mono text-text-muted">
              No activity logs recorded.
            </div>
          ) : (
            <div className="space-y-2 border-l border-surface-border ml-3 pl-4">
              {activities.map((act) => (
                <div key={act.id} className="relative font-mono text-xs space-y-1 py-1">
                  <div className="absolute -left-[21px] top-2 h-2 w-2 rounded-full bg-brand" />
                  <div className="flex items-center gap-2">
                    <Badge variant="outline" className="font-mono text-[9px]">
                      {act.activityType}
                    </Badge>
                    <span className="text-[10px] text-text-muted">
                      {new Date(act.createdAt).toLocaleString()}
                    </span>
                  </div>
                  <p className="text-text-primary">{act.description}</p>
                </div>
              ))}
            </div>
          )}
        </div>
      )}

      {/* Modal: Generate Document */}
      {showDocModal && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/60 backdrop-blur-sm p-4">
          <div className="w-full max-w-lg p-5 rounded-lg border border-surface-border bg-surface-card space-y-4">
            <div className="border-b border-surface-border pb-3">
              <h3 className="font-mono text-sm font-bold text-text-primary flex items-center gap-2">
                <Sparkles className="h-4 w-4 text-brand" /> Generate Tailored Application Document
              </h3>
              <p className="text-xs font-mono text-text-muted mt-1">
                Drafting for {application.title} at {application.organization}
              </p>
            </div>

            <form onSubmit={handleGenerateDocument} className="space-y-3 font-mono text-xs">
              <div>
                <label className="block text-text-muted mb-1">Document Type</label>
                <select
                  value={docType}
                  onChange={(e) => setDocType(e.target.value as DocumentType)}
                  className="w-full p-2 bg-bg-main border border-surface-border rounded text-xs font-mono text-text-primary focus:outline-none focus:border-brand"
                >
                  <option value="COVER_LETTER">Cover Letter</option>
                  <option value="GSOC_PROPOSAL">GSoC / Open-Source Proposal (5-Stage Architecture)</option>
                  <option value="MOTIVATION_STATEMENT">Motivation Statement</option>
                  <option value="APPLICATION_ESSAY">Application Essay</option>
                </select>
              </div>

              <div>
                <label className="block text-text-muted mb-1">Document Title (Optional)</label>
                <Input
                  value={docTitle}
                  onChange={(e) => setDocTitle(e.target.value)}
                  placeholder={`e.g. Cover Letter - ${application.organization}`}
                  className="font-mono text-xs bg-bg-main"
                />
              </div>

              <div>
                <label className="block text-text-muted mb-1">Custom Emphasis / Instructions (Optional)</label>
                <textarea
                  rows={3}
                  value={docInstructions}
                  onChange={(e) => setDocInstructions(e.target.value)}
                  placeholder="e.g. Emphasize backend microservices, past contributions to Apache projects..."
                  className="w-full p-2 bg-bg-main border border-surface-border rounded text-xs font-mono text-text-primary focus:outline-none focus:border-brand"
                />
              </div>

              <div className="flex items-center justify-end gap-2 pt-2 border-t border-surface-border">
                <Button
                  type="button"
                  variant="outline"
                  onClick={() => setShowDocModal(false)}
                  className="font-mono text-xs"
                >
                  Cancel
                </Button>
                <Button type="submit" disabled={generatingDoc} className="font-mono text-xs">
                  {generatingDoc ? "Drafting..." : "Generate with AI"}
                </Button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* Modal: Schedule Interview */}
      {showInterviewModal && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/60 backdrop-blur-sm p-4">
          <div className="w-full max-w-lg p-5 rounded-lg border border-surface-border bg-surface-card space-y-4">
            <div className="border-b border-surface-border pb-3">
              <h3 className="font-mono text-sm font-bold text-text-primary flex items-center gap-2">
                <Video className="h-4 w-4 text-brand" /> Schedule Assessment / Interview Round
              </h3>
              <p className="text-xs font-mono text-text-muted mt-1">
                {application.organization} • {application.title}
              </p>
            </div>

            <form onSubmit={handleScheduleInterview} className="space-y-3 font-mono text-xs">
              <div className="grid grid-cols-2 gap-3">
                <div>
                  <label className="block text-text-muted mb-1">Stage Type</label>
                  <select
                    value={stageType}
                    onChange={(e) => setStageType(e.target.value as InterviewStageType)}
                    className="w-full p-2 bg-bg-main border border-surface-border rounded text-xs font-mono text-text-primary focus:outline-none focus:border-brand"
                  >
                    <option value="ONLINE_ASSESSMENT">Online Assessment (OA)</option>
                    <option value="CODING_ROUND">Coding Round / DSA</option>
                    <option value="TECHNICAL_INTERVIEW">Technical System Design</option>
                    <option value="PROJECT_DISCUSSION">Project / Architecture Discussion</option>
                    <option value="HR_INTERVIEW">HR / Behavioral</option>
                    <option value="TAKE_HOME">Take-Home Assignment</option>
                  </select>
                </div>

                <div>
                  <label className="block text-text-muted mb-1">Duration (Mins)</label>
                  <Input
                    type="number"
                    value={duration}
                    onChange={(e) => setDuration(parseInt(e.target.value) || 60)}
                    className="font-mono text-xs bg-bg-main"
                  />
                </div>
              </div>

              <div>
                <label className="block text-text-muted mb-1">Scheduled Date & Time</label>
                <Input
                  type="datetime-local"
                  value={interviewDate}
                  onChange={(e) => setInterviewDate(e.target.value)}
                  className="font-mono text-xs bg-bg-main"
                />
              </div>

              <div>
                <label className="block text-text-muted mb-1">Meeting Link (Google Meet, Zoom, Portal)</label>
                <Input
                  type="url"
                  value={meetingLink}
                  onChange={(e) => setMeetingLink(e.target.value)}
                  placeholder="https://meet.google.com/..."
                  className="font-mono text-xs bg-bg-main"
                />
              </div>

              <div>
                <label className="block text-text-muted mb-1">Interviewer Name / Role (Optional)</label>
                <Input
                  value={interviewer}
                  onChange={(e) => setInterviewer(e.target.value)}
                  placeholder="e.g. Lead Distributed Systems Engineer"
                  className="font-mono text-xs bg-bg-main"
                />
              </div>

              <div>
                <label className="block text-text-muted mb-1">Preparation Target Notes</label>
                <textarea
                  rows={2}
                  value={prepNotes}
                  onChange={(e) => setPrepNotes(e.target.value)}
                  placeholder="Key topics to review: concurrency, tree algorithms, system design..."
                  className="w-full p-2 bg-bg-main border border-surface-border rounded text-xs font-mono text-text-primary focus:outline-none focus:border-brand"
                />
              </div>

              <div className="flex items-center justify-end gap-2 pt-2 border-t border-surface-border">
                <Button
                  type="button"
                  variant="outline"
                  onClick={() => setShowInterviewModal(false)}
                  className="font-mono text-xs"
                >
                  Cancel
                </Button>
                <Button type="submit" disabled={schedulingInterview} className="font-mono text-xs">
                  {schedulingInterview ? "Scheduling..." : "Save Interview"}
                </Button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
}
