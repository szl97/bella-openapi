// Server-side only configuration
export const workflow_url = process.env.WORKFLOW_API_URL || 'http://i-bella-workflow.ke.com/v1/workflow/run';
export const workflow_apikey = process.env.WORKFLOW_API_KEY;
export const es_url = process.env.ES_API_URL || 'http://api.fast.ke.com/es/query';
export const es_apikey = process.env.ES_API_KEY;