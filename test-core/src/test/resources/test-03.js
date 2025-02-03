// see EngineTest - keeping track of white space in template literals
var request = {
    body: `{
                "RequesterDetails": {
                    "InstructingTreasuryId": "000689",
                    "ApiRequestReference": "${idempotencyKey}",
                    "entity": "000689"
                 }
           }`
};
const foo = 'bar';