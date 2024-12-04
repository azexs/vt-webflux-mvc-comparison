import http from 'k6/http';
import { check } from 'k6';

export let options = {
    scenarios: {
        warmup: {
            executor: 'ramping-vus',
            startVUs: 0,
            stages: [
                { duration: '15s', target: 5000 },
            ],
            gracefulStop: '15s',
        },
        steady_load: {
            executor: 'constant-vus',
            vus: 5000,
            duration: '2m30s',
            startTime: '15s',
            gracefulStop: '15s',
        },
        rampdown: {
            executor: 'ramping-vus',
            startVUs: 5000,
            stages: [
                { duration: '15s', target: 0 },
            ],
            startTime: '2m45s',
            gracefulStop: '15s',
        },
    },
};

const endpoint = 'api/delay';

export default function () {
    const res = http.get(`http://localhost:8080/${endpoint}`);
    check(res, {
        'status is 200': (r) => r.status === 200,
    });
    return res;
}
