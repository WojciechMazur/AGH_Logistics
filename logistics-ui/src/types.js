// @flow
export class Node {
    id: string;
    name: string;
    available: ?number;
    priority: ?number;
    limit: ?number;

    constructor(
        id: string,
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
    purchaseCost: ?number;

    constructor(name: string,
                available: ?number,
                priority: ?number,
                limit: ?number,
                supply: number,
                purchaseCost: ?number) {
        super((Supplier.nextId += 1).toString(), name, available || supply, priority, limit);
        this.supply = supply;
        this.purchaseCost = purchaseCost;
    }

    static nextId: number = -1;
}

export class Recipient extends Node {
    demand: number;
    saleProfit: ?number;

    constructor(name: string,
                available:?number,
                priority: ?number,
                limit: ?number,
                demand: number,
                saleProfit: number
    ) {
        super((Recipient.nextId += 1).toString(), name, available || 0, priority, limit);
        this.demand = demand;
        this.saleProfit = saleProfit
    }
    static nextId: number = -1;
}



export class ConnectionAttributes {
    units: number;
    limit: ?number;
    priority: ?number;
    unitTransportCost: ?number;
    unitPurchaseCost: ?number;
    unitSaleProfit: ?number;

    constructor(units: number,
              limit: ?number,
              priority: ?number,
             unitTransportCost: ?number,
              unitPurchaseCost: ?number,
              unitSaleProfit: ?number) {
        this.units = units;
        this.limit = limit;
        this.priority = priority;
        this.unitTransportCost =unitTransportCost;
        this.unitPurchaseCost = unitPurchaseCost;
        this.unitSaleProfit = unitSaleProfit;
    }

    static default = new ConnectionAttributes(0, null, null, 0,null,null);
    static compare = (l: SimpleConnection, r: SimpleConnection) => {
        const leftName = (l.supplier.name + l.recipient.name).toLowerCase();
        const rightName = (r.supplier.name + r.recipient.name).toLowerCase();
        return leftName.localeCompare(rightName)
    };
}

export class SimpleConnection {
    id: string;
    supplier: Supplier;
    recipient: Recipient;
    attributes: ConnectionAttributes;
    constructor(
        supplier: Supplier,
        recipient: Recipient,
        attributes: ConnectionAttributes
    ) {
        this.id = (SimpleConnection.nextId += 1).toString();
        this.supplier = supplier;
        this.recipient = recipient;
        this.attributes = attributes;
    }

    static compare = (left:SimpleConnection, right:SimpleConnection) => {
        const leftName = left.supplier.name+left.recipient.name;
        const rightName = right.supplier.name+right.recipient.name;
        return leftName.toLowerCase().localeCompare(rightName.toLowerCase());
    };
    static nextId: number = -1;
}

export type Connection = {
    SimpleConnection: SimpleConnection,
    MediatorConnection: SimpleConnection
}
