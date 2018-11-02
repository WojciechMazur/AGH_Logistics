// @flow
export class Node {
    id: number | string;
    name: string;
    available: ?number;
    priority: ?number;
    limit: ?number;
    constructor(
        id: number | string,
        name: string,
        available: ?number,
        priority: ?number,
        limit: ?number
    ) {
        this.id = id;
        this.name = name;
        this.available = available;
        this.priority = priority;
        this.limit = limit;
    }
}


export class Supplier extends Node{
    supply: number;
    constructor(name: string,
                available: ?number,
                priority: ?number,
                limit: ?number,
                supply: number) {
        super(Supplier.nextId += 1, name, available || supply, priority, limit);
        this.supply = supply;
    }

    static nextId: number = -1;
}

export class Recipient extends Node {
    demand: number;
    constructor(name: string,
                available:?number,
                priority: ?number,
                limit: ?number,
                demand: number) {
        super(Recipient.nextId += 1, name, available || 0, priority, limit);
        this.demand = demand;
    }
    static nextId: number = -1;
}



export class ConnectionAttributes {
    units: number;
    limit: ?number;
    priority: ?number;
    transportCost: number;

    constructor(units: number,
                limit: ?number,
                priority: ?number,
                transportCost: number) {
        this.units = units;
        this.limit = limit;
        this.priority = priority;
        this.transportCost = transportCost;
    }

    static default = new ConnectionAttributes(0, null, null, 0);
    static compare = (l: Connection, r: Connection) => {
        const leftName = (l.supplier.name + l.recipient.name).toLowerCase();
        const rightName = (r.supplier.name + r.recipient.name).toLowerCase();
        return leftName.localeCompare(rightName)
    };
}

export class MediatorConnectionAttributes extends ConnectionAttributes {
    unitPurchaseCost: number;
    unitSaleProfit: number;

    constructor(units: number,
                limit: ?number,
                priority: ?number,
                transportCost: number,
                unitPurchaseCost: number,
                unitSaleProfit: number) {
        super(units, limit, priority, transportCost);
        this.unitPurchaseCost = unitPurchaseCost;
        this.unitSaleProfit = unitSaleProfit;
    }
}

export class Connection {
    id: number | string;
    supplier: Supplier;
    recipient: Recipient;
    attributes: ConnectionAttributes;
    constructor(
        supplier: Supplier,
        recipient: Recipient,
        attributes: ConnectionAttributes
    ) {
        this.id = Connection.nextId += 1;
        this.supplier = supplier;
        this.recipient = recipient;
        this.attributes = attributes;
    }

    static compare = (left:Connection, right:Connection) => {
        const leftName = left.supplier.name+left.recipient.name;
        const rightName = right.supplier.name+right.recipient.name;
        return leftName.toLowerCase().localeCompare(rightName.toLowerCase());
    };
    static nextId: number = -1;

}

